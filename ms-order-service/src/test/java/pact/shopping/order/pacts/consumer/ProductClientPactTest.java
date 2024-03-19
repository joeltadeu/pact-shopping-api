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
import pact.shopping.order.dto.CurrencyDto;
import pact.shopping.order.dto.PriceResponse;
import pact.shopping.order.dto.ProductResponse;
import pact.shopping.order.service.client.ProductClient;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles({"local"})
@SpringBootTest({
        // overriding provider address
        "feign.clients.product: http://localhost:8888"
})
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ProductService")
@MockServerConfig(hostInterface = "localhost", port = "8888")
public class ProductClientPactTest {

    @Autowired
    private ProductClient productClient;

    @Pact(consumer = "OrderService")
    public RequestResponsePact singleProduct(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return builder
                .given("product with ID 10 exists", "id", 10)
                .uponReceiving("product with ID 10 exists")
                .path("/v1/products/10")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(
                        new PactDslJsonBody()
                            .integerType("id", 10L)
                            .stringType("name", "Samsung TV Neo QLED 8K 85 QE85QN800B")
                            .stringType("category", "ELECTRONICS")
                            .integerType("quantity", 10)
                            .minArrayLike("prices", 1)
                                .integerType("id", 1L)
                                .numberType("amount", 145.78)
                                .numberType("discount", 0.0)
                                .object("currency")
                                    .stringType("symbol", "$")
                                    .stringType("iso", "USD")
                                .closeObject()
                            .closeObject()
                            .closeArray()

                )
                .toPact();
    }

    @Pact(consumer = "OrderService")
    public RequestResponsePact singleProductNotExists(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return builder
                .given("product with ID 10 does not exist", "id", 10)
                .uponReceiving("product with ID 10 does not exist")
                .path("/v1/products/10")
                .willRespondWith()
                .headers(headers)
                .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "singleProduct")
    void testSingleProduct() {
        Optional<ProductResponse> optionalProduct = productClient.findById(10L);

        assertThat(optionalProduct.orElseGet(ProductResponse::new), is(equalTo(ProductResponse.builder()
                .id(10L)
                .name("Samsung TV Neo QLED 8K 85 QE85QN800B")
                .category("ELECTRONICS")
                .quantity(10)
                .prices(Collections.singletonList(PriceResponse.builder()
                                .amount(BigDecimal.valueOf(145.78))
                                .discount(BigDecimal.valueOf(0.0))
                                .currency(CurrencyDto.builder()
                                        .symbol("$")
                                        .iso("USD")
                                        .build())
                        .build()))
                .build())));

    }

    @Test
    @PactTestFor(pactMethod = "singleProductNotExists")
    void testSingleProductNotExists() {
        try {
            productClient.findById(10L);
            fail("Expected service call to throw an exception");
        } catch (FeignException ex) {
            assertThat(ex.getMessage(), containsString("Not Found"));
        }
    }
}
