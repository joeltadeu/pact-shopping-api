package pact.shopping.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pact.shopping.product.dto.ProductResponse;
import pact.shopping.product.model.Product;
import pact.shopping.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController implements IController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody
            @Valid Product request) {
        var product = service.insert(request);
        return ResponseEntity.created(getURI(product.getId())).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable
            Long id,
            @RequestBody
            @Valid @NotNull Product request) {
        request.setId(id);
        service.update(request);
        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable
            Long id) {

        final var product = service.findByIdWithPrices(id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable
            Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> listAll() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }
}
