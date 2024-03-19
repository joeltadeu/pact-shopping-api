package pact.shopping.price.service;

import org.springframework.stereotype.Service;
import pact.shopping.price.model.Price;
import pact.shopping.price.repository.PriceRepository;
import pact.shopping.price.service.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PriceService {
    private final PriceRepository repository;

    public PriceService(PriceRepository repository) {
        this.repository = repository;
    }

    public Price insert(Price price) {
        price.setUuid(UUID.randomUUID().toString());
        price.setCreatedAt(LocalDateTime.now());
        return repository.save(price);
    }

    public void update(Price price) {
        var priceFound = findById(price.getId());
        priceFound.setAmount(price.getAmount());
        repository.save(priceFound);
    }

    public Price findById(Long id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<Price> findAllByProductId(Long productId) {
        return repository.findAllByProductId(productId);
    }

    public void delete(Long id) {
        Price price = findById(id);
        repository.delete(price);
    }
}
