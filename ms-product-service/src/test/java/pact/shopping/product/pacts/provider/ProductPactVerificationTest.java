package pact.shopping.product.pacts.provider;

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
import pact.shopping.product.dto.CurrencyDto;
import pact.shopping.product.dto.CurrencyEnum;
import pact.shopping.product.dto.PriceResponse;
import pact.shopping.product.model.Product;
import pact.shopping.product.model.enums.ProductCategoryEnum;
import pact.shopping.product.repository.ProductRepository;
import pact.shopping.product.service.client.PriceClient;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Provider("ProductService")
@PactBroker
@IgnoreNoPactsToVerify
@VerificationReports
public class ProductPactVerificationTest {

    @LocalServerPort
    int port;

    @MockBean
    ProductRepository productRepository;

    @MockBean
    PriceClient priceClient;

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

    @State(value = "product with ID 10 exists", action = StateChangeAction.SETUP)
    void productExists(Map<String, Object> params) {
        long productId = ((Number) params.get("id")).longValue();
        Product product = Product.builder()
                .id(productId)
                .name("Samsung TV Neo QLED 8K 85 QE85QN800B")
                .category(ProductCategoryEnum.ELECTRONICS)
                .createdAt(LocalDateTime.now())
                .quantity(10)
                .build();

        var prices = Collections.singletonList(PriceResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(145.78))
                .discount(BigDecimal.valueOf(0.0))
                .currency(CurrencyDto.builder()
                        .iso(CurrencyEnum.USD)
                        .symbol("$").build())
                .build());

        when(productRepository.findById(eq(10L))).thenReturn(Optional.of(product));
        when(priceClient.findAllByProductId(eq(10L))).thenReturn(prices);
        when(productRepository.save(any(Product.class))).thenReturn(product);
    }

    @State(value = "product with ID 10 does not exist", action = StateChangeAction.SETUP)
    void productNotExist(Map<String, Object> params) {
        long productId = ((Number) params.get("id")).longValue();
        when(productRepository.findById(eq(productId))).thenReturn(Optional.empty());
    }
}
