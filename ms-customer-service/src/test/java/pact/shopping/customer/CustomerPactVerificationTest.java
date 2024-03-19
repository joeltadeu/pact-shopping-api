package pact.shopping.customer;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.*;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import pact.shopping.customer.model.Customer;
import pact.shopping.customer.repository.CustomerRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    @State(value = "customer with ID 10 does not exist", action = StateChangeAction.SETUP)
    void customerNotExist(Map<String, Object> params) {
        long customerId = ((Number) params.get("id")).longValue();
        when(customerRepository.findById(eq(customerId))).thenReturn(Optional.empty());
    }
}
