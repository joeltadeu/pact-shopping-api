package pact.shopping.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pact.shopping.order.dto.CustomerResponse;
import pact.shopping.order.dto.ProductResponse;

import java.util.Optional;

@Component
@FeignClient(url="${feign.clients.customer}", name="CustomerClient")
public interface CustomerClient {

    @GetMapping(value = "/v1/customers/{id}")
    Optional<CustomerResponse> findById(@PathVariable("id") Long id);
}
