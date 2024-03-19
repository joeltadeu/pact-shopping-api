package pact.shopping.product.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pact.shopping.product.dto.ProductResponse;
import pact.shopping.product.mapping.ProductMapping;
import pact.shopping.product.model.Product;
import pact.shopping.product.repository.ProductRepository;
import pact.shopping.product.service.client.PriceClient;
import pact.shopping.product.service.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapping mapper;
    private final PriceClient priceClient;


    public Product insert(Product product) {
        product.setActive(Boolean.TRUE);
        product.setUuid(UUID.randomUUID().toString());
        product.setCreatedAt(LocalDateTime.now());
        return repository.save(product);
    }

    public void update(Product product) {
        var productFound = findById(product.getId());
        productFound.setName(product.getName());
        productFound.setCategory(product.getCategory());
        productFound.setQuantity(product.getQuantity());
        repository.save(productFound);
    }

    public ProductResponse findByIdWithPrices(Long id) {
        var product = repository.findById(id).orElseThrow(NotFoundException::new);
        var prices = priceClient.findAllByProductId(id);
        return mapper.to(product, prices);
    }

    public Product findById(Long id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        Product product = findById(id);
        product.setActive(Boolean.FALSE);
        repository.save(product);
    }
}
