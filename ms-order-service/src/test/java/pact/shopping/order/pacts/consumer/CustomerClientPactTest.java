package pact.shopping.order.pacts.consumer;


import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pact.shopping.order.dto.CustomerResponse;
import pact.shopping.order.service.client.CustomerClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles({"local"})
@SpringBootTest({
        // overriding provider address
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

    @Pact(consumer = "OrderService")
    public RequestResponsePact singleCustomerNotExists(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return builder
                .given("customer with ID 10 does not exist", "id", 10)
                .uponReceiving("customer with ID 10 does not exist")
                .path("/v1/customers/10")
                .willRespondWith()
                .headers(headers)
                .status(404)
                .toPact();
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

    @Test
    @PactTestFor(pactMethod = "singleCustomerNotExists")
    void testSingleCustomerNotExists() {
        try {
            customerClient.findById(10L);
            fail("Expected service call to throw an exception");
        } catch (FeignException ex) {
            assertThat(ex.getMessage(), containsString("Not Found"));
        }
    }
}
