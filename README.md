# pact-shopping-api

## Overview

This project is aimed at creating a Consumer-Driven Contract with [Feign](https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-feign.html) and [Pact](https://pact.io/)

## Architecture

![Alt text](_assets/architecture/pact-shopping.png?raw=true "Shopping Architecture")

## Structure

The following list of the main components of the project:

| Project                                              | Description                                       |
|------------------------------------------------------|---------------------------------------------------|
| [ms-customer-service](ms-customer-service/README.md) | Microservice responsible for customer management. |
| [ms-order-service](ms-order-service/README.md)       | Microservice responsible for order management.    |
| [ms-price-service](ms-price-service/README.md)       | Microservice responsible for price management.    |
| [ms-product-service](ms-product-service/README.md)   | Microservice responsible for product management.  |


## Consumer-Driven Contract

Consumer-driven contract tests are a technique to test integration points between API providers and API consumers without the hassle of end-to-end tests. A common use case for consumer-driven contract tests is testing interfaces between services in a microservice architecture. In the Java ecosystem, Feign in combination with Spring Boot is a popular stack for creating API clients in a distributed architecture. Pact is a polyglot framework that facilitates consumer-driven contract tests. So let’s have a look at how to create a contract with Feign and Pact and test a Feign client against that contract.

### Define the contract

A contract is called a “pact” within the Pact framework. In order to create a pact we need to include the pact library

```xml
<dependency>
    <groupId>au.com.dius.pact.consumer</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.7</version>
    <scope>test</scope>
</dependency>
```
As the name suggests, we’re generating a contract from a JUnit5 unit test.

Create a test class called `CustomerClientPactTest` that is going to create a pact for us:

```java
@ExtendWith(PactConsumerTestExt.class)
public class CustomerClientPactTest {
    
    @Pact(consumer = "OrderService")
    public RequestResponsePact singleCustomer(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return builder
                .given("customer with ID 10 exists", "id", 10)
                .uponReceiving("customer with ID 10 exists")
                .path("/v1/customers/10")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(
                    new PactDslJsonBody()
                        .integerType("id", 10L)
                        .stringType("uuid", "cb1fbbe6-d81b-476c-93a9-aa239150891e")
                        .stringType("firstName", "John")
                        .stringType("lastName", "Fox")
                        .stringType("email", "john.fox@gmail.com")
                        .booleanType("active", true)
                )
                .toPact();
    }
}
```
This method defines a single interaction between a consumer and a provider, called a “fragment” of a pact. A test class can contain multiple such fragments which together make up a complete pact.

The fragment we’re defining here should define the use case of getting a Customer resource.

The `@Pact` annotation tells Pact that we want to define a pact fragment. It contains the names of the consumer and the provider to uniquely identify the contract partners.

### Create a Client against the API

Before we can verify a client, we have to create it first.

We choose Feign as the technology to create a client against the API defined in the contract.

We need to add the Feign dependency to the maven build:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

Next, we create the actual client and the data classes used in the API:

```java
@Component
@FeignClient(url="${feign.clients.customer}", name="CustomerClient")
public interface CustomerClient {

    @GetMapping(value = "/v1/customers/{id}")
    Optional<CustomerResponse> findById(@PathVariable("id") Long id);
}
```

The `@FeignClient` annotation tells Spring Boot to create an implementation of the `CustomerClient` interface that should run against the host that configured under the variable `${feign.clients.customer}` defined in the property file.

For the Feign client to work, we need to add the `@EnableFeignClients` to our application class

```java
@SpringBootApplication
@EnableFeignClients
public class OrderApplication {
    ...
}
```

### Verify the Client against the Contract (Consumer)

Let’s go back to our JUnit test class `CustomerClientPactTest` and extend it so that it verifies that the Feign client we just created actually works as defined in the contract:

```java
@ActiveProfiles({"local"})
@SpringBootTest({
   "feign.clients.customer: http://localhost:8888"
})
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "CustomerService")
@MockServerConfig(hostInterface = "localhost", port = "8888")
public class CustomerClientPactTest {

    @Autowired
    private CustomerClient customerClient;

    @Pact(consumer = "OrderService")
    public RequestResponsePact singleCustomer(PactDslWithProvider builder) {
        ... // see code to create a pact
    }
    
    @Test
    @PactTestFor(pactMethod = "singleCustomer")
    void testSingleCustomer() {
        Optional<CustomerResponse> optionalCustomer = customerClient.findById(10L);

        assertThat(optionalCustomer.orElseGet(CustomerResponse::new), is(equalTo(CustomerResponse.builder()
                .id(10L)
                .firstName("John")
                .lastName("Fox")
                .email("john.fox@gmail.com")
                .build())));

    }
```

We start off by using the standard `@SpringBootTest` annotation together with the `SpringExtension` for JUnit 5. Important to note is that we mock our client sends its requests against `localhost:8888`.

With the PactConsumerTestExt together with the `@PactTestFor` annotation, we tell pact to start a mock API provider on `localhost:8888`. This mock provider will return responses according to all pact fragments from the `@Pact` methods within the test class.

The actual verification of our Feign client is implemented in the method `testSingleCustomer()`. The `@PactTestFor` annotation defines which pact fragment we want to test (the fragment property must be the name of a method annotated with `@Pact` within the test class).

If the request the client sends to the mock provider looks as defined in the pact, the according response will be returned and the test will pass. If the client does something differently, the test will fail, meaning that we do not meet the contract.

Once the test has passed, a pact file with the name `OrderService-CustomerService.json` will be created in the `target/pacts` folder.

### Publish the Contract to a Pact Broker

The Pact Broker is an application for sharing consumer driven contracts and verification results. It is optimised for use with "pacts" (contracts created by the Pact framework), but can be used for any type of contract that can be serialized to JSON.

Running the Pact Broker with docker-compose:

```bash                                                                                                                                                                                                                                                                                                   0.2s 
pact-shopping-api> docker-compose up  
[+] Running 3/3
 ✔ Network pact-shopping-api_default          Created                                                                                                                                                                                                                                                                                                      0.0s 
 ✔ Container pact-shopping-api-postgres-1     Created                                                                                                                                                                                                                                                                                                      0.1s 
 ✔ Container pact-shopping-api-pact-broker-1  Created                                                                                                                                                                                                                                                                                                      0.0s 
Attaching to pact-broker-1, postgres-1
postgres-1     | The files belonging to this database system will be owned by user "postgres".
postgres-1     | This user must also own the server process.
postgres-1     |
postgres-1     | The database cluster will be initialized with locale "en_US.utf8".
postgres-1     | The default database encoding has accordingly been set to "UTF8".
postgres-1     | The default text search configuration will be set to "english".
postgres-1     |
postgres-1     | Data page checksums are disabled.
postgres-1     |
postgres-1     | PostgreSQL init process complete; ready for start up.
postgres-1     |
postgres-1     | 2024-03-17 12:31:22.495 UTC [1] LOG:  starting PostgreSQL 16.2 (Debian 16.2-1.pgdg120+2) on x86_64-pc-linux-gnu, compiled by gcc (Debian 12.2.0-14) 12.2.0, 64-bit
postgres-1     | 2024-03-17 12:31:22.495 UTC [1] LOG:  listening on IPv4 address "0.0.0.0", port 5432
postgres-1     | 2024-03-17 12:31:22.496 UTC [1] LOG:  listening on IPv6 address "::", port 5432
postgres-1     | 2024-03-17 12:31:22.502 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
pact-broker-1  | 2024-03-17 12:31:25.473860 I [7:3320] pact-broker -- Connected to database postgres
pact-broker-1  | 2024-03-17 12:31:25.478398 I [7:3320] PactBroker::App -- Database schema version is 0
pact-broker-1  | 2024-03-17 12:31:25.524054 I [7:3320] PactBroker::App -- Migrating database schema
pact-broker-1  | 2024-03-17 12:31:26.674367 I [7:3320] PactBroker::App -- Database schema version is now 20231003
pact-broker-1  | 2024-03-17 12:31:26.674405 I [7:3320] PactBroker::App -- Migrating data
pact-broker-1  | 2024-03-17 12:31:27.385963 I [7:3320] PactBroker::App -- Marking seed as done
pact-broker-1  | * Listening on http://0.0.0.0:9292
pact-broker-1  | 2024-03-17 12:31:27.388739 I [7:3320] pact-broker -- ------------------------------------------------------------------------
pact-broker-1  | 2024-03-17 12:31:27.388763 I [7:3320] pact-broker -- PACT BROKER CONFIGURATION:
pact-broker-1  | 2024-03-17 12:31:27.389554 I [7:3320] pact-broker -- webhook_scheme_whitelist=["http"] source={:type=>:env, :key=>"PACT_BROKER_WEBHOOK_SCHEME_WHITELIST"}
pact-broker-1  | 2024-03-17 12:31:27.389559 I [7:3320] pact-broker -- ------------------------------------------------------------------------
pact-broker-1  | 2024-03-17 12:31:27.389566 I [7:3320] PactBroker::App --
pact-broker-1  |
pact-broker-1  | ********************************************************************************
pact-broker-1  |
pact-broker-1  | Want someone to manage your Pact Broker for you? Check out https://pactflow.io/oss for a hardened, fully supported SaaS version of the Pact Broker with an improved UI + more.
pact-broker-1  |
pact-broker-1  | ********************************************************************************
```

Access the Pact broker server with:

| Url         | http://localhost:9292 |
|-------------|-----------------------|
| Username    | pact_shopping         |
| Password    | pact_shopping         |

The pact file created from our test now has to be made available to the provider side so that the provider can also test against the contract.

Pacts provides a Maven plugin that we can use for this purpose:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>au.com.dius.pact.provider</groupId>
            <artifactId>maven</artifactId>
            <version>4.6.7</version>
            <configuration>
                <pactBrokerUrl>http://localhost:9292</pactBrokerUrl>
                <pactBrokerUsername>pact_shopping</pactBrokerUsername>
                <pactBrokerPassword>pact_shopping</pactBrokerPassword>
            </configuration>
        </plugin>
    </plugins>
</build>
```

We can now run `./mvnw pact:publish` to publish all pacts generated from our tests to the specified Pact Broker. The API provider can get the pact from there to validate his own code against the contract.

![Alt text](_assets/pact/pact_broker_pacts_deployed.png?raw=true "Pact deployed")

### Verify contracts on Provider

All we need to do for the provider is update the test where it finds its pacts, from local URLs, to one from a broker.

First, add the pact provider dependency library in the `pom.xml` of your provider project:

```xml
 <dependency>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>junit5spring</artifactId>
    <version>4.6.7</version>
</dependency>
```

Add the pact provider plugin in our `pom.xml`. This is a Maven plugin for verifying pacts against a running provider, publishing pacts generated by consumer tests, and checking if you can deploy

```xml
<build>
    <plugins>
        <plugin>
            <groupId>au.com.dius.pact.provider</groupId>
            <artifactId>maven</artifactId>
            <version>4.6.7</version>
            <configuration>
                <systemPropertyVariables>
                    <pact.showStacktrace>true</pact.showStacktrace>
                    <pact.verifier.publishResults>true</pact.verifier.publishResults>
                </systemPropertyVariables>
                <serviceProviders>
                    <serviceProvider>
                        <name>CustomerService</name>
                        <protocol>http</protocol>
                        <host>localhost</host>
                        <port>9292</port>
                        <pactBroker>
                            <url>http://localhost:9292</url>
                            <authentication>
                                <scheme>basic</scheme>
                                <username>pact_shopping</username>
                                <password>pact_shopping</password>
                            </authentication>
                        </pactBroker>
                    </serviceProvider>
                </serviceProviders>
            </configuration>
        </plugin>
    </plugins>
</build>
```

We add a `@PactBroker` annotation to our test and change it to use the `PactVerificationSpringProvider`,
and then create a test application YAML configuration file with the details of the Pact Broker.

In `ms-customer-service/src/test/java/pact/shopping/customer/CustomerPactVerificationTest.java`:

```java
@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Provider("CustomerService")
@PactBroker
@IgnoreNoPactsToVerify
@VerificationReports
public class CustomerPactVerificationTest {

    @LocalServerPort
    int port;

    @MockBean
    CustomerRepository customerRepository;

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
            System.setProperty("pact.verifier.publishResults", "true");
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        if (context != null)
            context.setTarget(new HttpTestTarget("localhost", port));
    }

    @State(value = "customer with ID 10 exists", action = StateChangeAction.SETUP)
    void customerExists(Map<String, Object> params) {
        long customerId = ((Number) params.get("id")).longValue();
        Customer customer = Customer.builder()
                .id(customerId)
                .uuid("cb1fbbe6-d81b-476c-93a9-aa239150891e")
                .firstName("John")
                .lastName("Fox")
                .email("john.fox@gmail.com")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        when(customerRepository.findById(eq(10L))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
    }
```

and then create `ms-customer-service/src/main/resources/application-test.yml`:

```yaml
pactbroker:
  host: localhost
  port: "9292"
  auth:
    username: pact_shopping
    password: pact_shopping
```

Let's run the provider verification one last time after this change:

```bash
ms-price-service> mvn verify

<<< Omitted >>>

Verifying a pact between OrderService (0.0.1-SNAPSHOT) and CustomerService

  Notices:
    1) The pact at http://localhost:9292/pacts/provider/CustomerService/consumer/OrderService/pact-version/32ec4d1ff65ca007d2dc9d8f5c089df5fb85aacf is being verified because the pact content belongs to the consumer version matching the following criterion:
    * latest version of OrderService that has a pact with CustomerService (0.0.1-SNAPSHOT)

  [from Pact Broker http://localhost:9292/pacts/provider/CustomerService/consumer/OrderService/pact-version/32ec4d1ff65ca007d2dc9d8f5c089df5fb85aacf/metadata/c1tdW2xdPXRydWUmc1tdW2N2XT05]
  Given customer with ID 10 does not exist
  customer with ID 10 does not exist
2024-03-18T20:39:32.644-03:00  WARN 19264 --- [customer-service] [       Thread-3] au.com.dius.pact.core.support.Metrics    :
            Please note: we are tracking events anonymously to gather important usage statistics like JVM version
            and operating system. To disable tracking, set the 'pact_do_not_track' system property or environment
            variable to 'true'.

2024-03-18T20:39:32.687-03:00  INFO 19264 --- [customer-service] [nio-9081-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2024-03-18T20:39:32.688-03:00  INFO 19264 --- [customer-service] [nio-9081-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2024-03-18T20:39:32.689-03:00  INFO 19264 --- [customer-service] [nio-9081-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
    returns a response which
      has status code 404 (OK)
      has a matching body (OK)
2024-03-18T20:39:32.911-03:00  WARN 19264 --- [customer-service] [           main] a.c.d.p.p.DefaultTestResultAccumulator   : Not all of the 2 were verified. The following were missing:
2024-03-18T20:39:32.912-03:00  WARN 19264 --- [customer-service] [           main] a.c.d.p.p.DefaultTestResultAccumulator   :     customer with ID 10 exists
2024-03-18T20:39:32.927-03:00  INFO 19264 --- [customer-service] [           main] rificationStateChangeExtension$Companion : Invoking state change method 'customer with ID 10 exists':SETUP

Verifying a pact between OrderService (0.0.1-SNAPSHOT) and CustomerService

  Notices:
    1) The pact at http://localhost:9292/pacts/provider/CustomerService/consumer/OrderService/pact-version/32ec4d1ff65ca007d2dc9d8f5c089df5fb85aacf is being verified because the pact content belongs to the consumer version matching the following criterion:
    * latest version of OrderService that has a pact with CustomerService (0.0.1-SNAPSHOT)

  [from Pact Broker http://localhost:9292/pacts/provider/CustomerService/consumer/OrderService/pact-version/32ec4d1ff65ca007d2dc9d8f5c089df5fb85aacf/metadata/c1tdW2xdPXRydWUmc1tdW2N2XT05]
  Given customer with ID 10 exists
  customer with ID 10 exists
    returns a response which
      has status code 200 (OK)
      has a matching body (OK)
2024-03-18T20:39:32.965-03:00  WARN 19264 --- [customer-service] [           main] a.c.dius.pact.provider.ProviderVersion   : Provider version not set, defaulting to '0.0.0'
2024-03-18T20:39:33.042-03:00  INFO 19264 --- [customer-service] [           main] a.c.d.p.p.DefaultVerificationReporter    : Published verification result of 'Ok(interactionIds=[5b41a600955b70e2b827a0e15616486c5dab00c9, 6f3e2aa8f70942ae31286ec679d1b9263a1c104c])' for consumer 'Consumer(name=OrderService)'
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.356 s -- in pact.shopping.customer.CustomerPactVerificationTest
[INFO] 
[INFO] Results:
[INFO]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-jar-plugin:3.3.0:jar (default-jar) @ customer ---
[INFO] 
[INFO] --- spring-boot-maven-plugin:3.2.3:repackage (repackage) @ customer ---
[INFO] Replacing main artifact C:\development\sourcecode\pact-shopping-api\ms-customer-service\target\customer-0.0.1-SNAPSHOT.jar with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to C:\development\sourcecode\pact-shopping-api\ms-customer-service\target\customer-0.0.1-SNAPSHOT.jar.original
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  21.199 s
[INFO] Finished at: 2024-03-18T20:39:34-03:00
[INFO] ------------------------------------------------------------------------
```

Open pact broker again and you will see that it was verified

![Alt text](_assets/pact/pact_broker_pacts_verified.png?raw=true "Pact Verified")

Click in the matrix button ![Alt text](_assets/pact/matrix_button.png) to see more details about the customer, provider and a link for the results

![Alt text](_assets/pact/pact_broker_matrix.png?raw=true "Pact Matrix")

In the results link you have details about the verification result for Pact between the customer and provider

![Alt text](_assets/pact/pact_broker_results.png?raw=true "Pact Results")








## Version

### 1.0.0

- Spring Boot 3.2
- Java 17
- MySQL 8
- Docker Compose
- Contract Tests
- Pact

## License
Apache License v2.0




