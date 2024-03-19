package pact.shopping.price.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pact.shopping.price.model.Price;
import pact.shopping.price.service.PriceService;

import java.util.List;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor

public class PriceController implements IController {

    private final PriceService service;

    @PostMapping("/{productId}/prices")
    public ResponseEntity<Price> create(
            @PathVariable
            Long productId,
            @RequestBody
            @Valid Price request) {
        request.setProductId(productId);
        var price = service.insert(request);
        return ResponseEntity.created(getURI(price.getId())).body(price);
    }

    @PutMapping("/{productId}/prices/{id}")
    public ResponseEntity<Void> update(
            @PathVariable
            Long productId,
            @PathVariable
            Long id,
            @RequestBody
            @Valid @NotNull Price request) {
        request.setId(id);
        request.setProductId(productId);
        service.update(request);
        return ResponseEntity.ok()
                .build();
    }

    @GetMapping("/{productId}/prices")
    public ResponseEntity<List<Price>> findAll(
            @PathVariable
            Long productId) {

        final var price = service.findAllByProductId(productId);
        return ResponseEntity.ok(price);
    }
}
