package pact.shopping.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pact.shopping.order.dto.ProductResponse;

import java.util.Optional;

@Component
@FeignClient(url="${feign.clients.product}", name = "ProductClient")
public interface ProductClient {

    @GetMapping(value = "/v1/products/{id}")
    Optional<ProductResponse> findById(@PathVariable("id") Long id);
}
