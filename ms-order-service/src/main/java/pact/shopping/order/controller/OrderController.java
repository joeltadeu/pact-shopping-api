package pact.shopping.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pact.shopping.order.dto.OrderRequest;
import pact.shopping.order.dto.OrderResponse;
import pact.shopping.order.service.OrderService;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class OrderController implements IController {

    private final OrderService service;

    @PostMapping("/{customerId}/orders")
    public ResponseEntity<OrderResponse> create(
            @PathVariable Long customerId,
            @RequestBody
            @Valid OrderRequest request) {

        var response = service.save(request, customerId);
        return ResponseEntity.created(getURI(response.getId())).body(response);
    }

    @GetMapping("/{customerId}/orders/{id}")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable Long customerId,
            @PathVariable
            Long id) {

        final var response = service.findByCustomerIdAndId(customerId, id);
        return ResponseEntity.ok(response);
    }

}
