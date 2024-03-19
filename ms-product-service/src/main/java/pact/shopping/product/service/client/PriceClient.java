package pact.shopping.product.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pact.shopping.product.dto.PriceResponse;

import java.util.List;

@Component
@FeignClient(url="${feign.clients.price}", name = "PriceClient")
public interface PriceClient {

    @GetMapping(value = "/v1/products/{productId}/prices")
    List<PriceResponse> findAllByProductId(@PathVariable("productId") Long id);
}
