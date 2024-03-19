package pact.shopping.price.pacts.provider;

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
import pact.shopping.price.model.CurrencyEnum;
import pact.shopping.price.model.Price;
import pact.shopping.price.model.PriceCurrency;
import pact.shopping.price.repository.PriceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Provider("PriceService")
@PactBroker
@IgnoreNoPactsToVerify
@VerificationReports
public class PricePactVerificationTest {

    @LocalServerPort
    int port;

    @MockBean
    PriceRepository priceRepository;

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

    @State(value = "prices by product ID 10 exists", action = StateChangeAction.SETUP)
    void pricesExists(Map<String, Object> params) {
        long productId = ((Number) params.get("id")).longValue();
        Price price = Price.builder()
                .id(1L)
                .uuid("a881884f-71e6-4d54-90df-39f8a33b3456")
                .productId(productId)
                .createdAt(LocalDateTime.parse("2024-03-09T19:44:17"))
                .amount(BigDecimal.valueOf(145.78))
                .discount(BigDecimal.valueOf(0.0))
                .currency(PriceCurrency.builder()
                        .iso(CurrencyEnum.USD)
                        .symbol("$")
                        .build())
                .build();

        when(priceRepository.findAllByProductId(eq(10L))).thenReturn(Collections.singletonList(price));
        when(priceRepository.save(any(Price.class))).thenReturn(price);
    }

    @State(value = "prices by product ID 10 does not exist", action = StateChangeAction.SETUP)
    void pricesNotExist(Map<String, Object> params) {
        long productId = ((Number) params.get("id")).longValue();
        when(priceRepository.findAllByProductId(eq(productId))).thenReturn(Collections.emptyList());
    }
}
