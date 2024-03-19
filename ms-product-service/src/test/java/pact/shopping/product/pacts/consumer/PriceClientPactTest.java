package pact.shopping.product.pacts.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pact.shopping.product.dto.CurrencyDto;
import pact.shopping.product.dto.CurrencyEnum;
import pact.shopping.product.dto.PriceResponse;
import pact.shopping.product.service.client.PriceClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"local"})
@SpringBootTest({
        // overriding provider address
        "feign.clients.price: http://localhost:8889"
})
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "PriceService")
@MockServerConfig(hostInterface = "localhost", port = "8889")
public class PriceClientPactTest {

    @Autowired
    private PriceClient priceClient;

    @Pact(consumer = "ProductService")
    public RequestResponsePact pricesByProduct(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return builder
                .given("prices by product ID 10 exists", "id", 10)
                .uponReceiving("price by product ID 10 exists")
                .path("/v1/products/10/prices")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(
                    """
                        [
                            {
                                "id": 1,
                                "uuid": "a881884f-71e6-4d54-90df-39f8a33b3456",
                                "createdAt": "2024-03-09T19:44:17",
                                "productId": 10,
                                "discount": 0.0,
                                "amount": 145.78,
                                "currency": {
                                    "symbol": "$",
                                    "iso": "USD"
                                }
                            }
                        ]
                            """
                )
                .toPact();
    }

    @Pact(consumer = "ProductService")
    public RequestResponsePact pricesByProductNotExists(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return builder
                .given("prices by product ID 10 does not exist", "id", 10)
                .uponReceiving("price by product ID 10 does not exist")
                .path("/v1/products/10/prices")
                .willRespondWith()
                .headers(headers)
                .status(200)
                .body("[]")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "pricesByProduct")
    void testPricesByProduct() {
        List<PriceResponse> prices = priceClient.findAllByProductId(10L);

        assertNotNull(prices);
        assertEquals(1, prices.size());

        var price = prices.get(0);

        assertThat(price, is(equalTo(PriceResponse.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(145.78))
                .discount(BigDecimal.valueOf(0.0))
                .currency(CurrencyDto.builder()
                        .iso(CurrencyEnum.USD)
                        .symbol("$")
                        .build())

                .build())));

    }

    @Test
    @PactTestFor(pactMethod = "pricesByProductNotExists")
    void testPricesByProductNotExists() {
        List<PriceResponse> prices = priceClient.findAllByProductId(10L);
        assertThat(prices, is(empty()));
    }
}
