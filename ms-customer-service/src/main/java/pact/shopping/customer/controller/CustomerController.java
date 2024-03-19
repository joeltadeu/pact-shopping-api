package pact.shopping.customer.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pact.shopping.customer.model.Customer;
import pact.shopping.customer.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController implements IController {

    private final CustomerService service;

    @PostMapping
    public ResponseEntity<Customer> create(
            @RequestBody
            @Valid Customer request) {
        var product = service.insert(request);
        return ResponseEntity.created(getURI(product.getId())).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable
            Long id,
            @RequestBody
            @Valid @NotNull Customer request) {
        request.setId(id);
        service.update(request);
        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> findById(
            @PathVariable
            Long id) {

        final var product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    public ResponseEntity<Void> delete(
            @PathVariable
            Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Customer>> listAll() {
        List<Customer> customers = service.findAll();
        return ResponseEntity.ok(customers);
    }
}
