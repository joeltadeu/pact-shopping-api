package pact.shopping.customer.service;

import org.springframework.stereotype.Service;
import pact.shopping.customer.model.Customer;
import pact.shopping.customer.repository.CustomerRepository;
import pact.shopping.customer.service.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer insert(Customer customer) {
        customer.setActive(Boolean.TRUE);
        customer.setUuid(UUID.randomUUID().toString());
        customer.setCreatedAt(LocalDateTime.now());
        return repository.save(customer);
    }

    public void update(Customer customer) {
        var productFound = findById(customer.getId());
        productFound.setFirstName(customer.getFirstName());
        productFound.setLastName(customer.getLastName());
        productFound.setEmail(customer.getEmail());
        repository.save(productFound);
    }

    public Customer findById(Long id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        Customer customer = findById(id);
        customer.setActive(Boolean.FALSE);
        repository.save(customer);
    }
}
