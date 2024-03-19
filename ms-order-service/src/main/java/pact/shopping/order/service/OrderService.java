package pact.shopping.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pact.shopping.order.dto.*;
import pact.shopping.order.mapping.OrderMapping;
import pact.shopping.order.model.Order;
import pact.shopping.order.model.OrderItem;
import pact.shopping.order.repository.OrderRepository;
import pact.shopping.order.service.client.CustomerClient;
import pact.shopping.order.service.client.ProductClient;
import pact.shopping.order.service.exceptions.BadRequestException;
import pact.shopping.order.service.exceptions.NotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final ProductClient productClient;
    private final CustomerClient customerClient;
    private final OrderMapping mapper;

    public OrderResponse save(OrderRequest request, Long customerId) {

        var customer = customerClient.findById(customerId).orElseThrow();
        var order = mapper.to(request, customerId);

        order.getItems().forEach(item -> {
            var product = getProduct(item);

            if (product.getQuantity() - item.getQuantity() < 0) {
                throw new BadRequestException("Product id %s not available on stock".formatted(item.getProductId()));
            }
            var price = mapper.to(product.getCurrentPrice());
            item.setPrice(price);
        });

        repository.save(order);

        return enhanceWithProductAndPriceDetails(order, customer);
    }

    public OrderResponse findByCustomerIdAndId(Long customerId, Long id) {
        var order = repository.findByCustomerIdAndId(customerId, id)
                .orElseThrow(() -> new NotFoundException("Order id '%s' for the customer '%s' not found".formatted(id, customerId)));
        var customer = customerClient.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer id'%s' not found".formatted(customerId)));
        return enhanceWithProductAndPriceDetails(order, customer);
    }

    private OrderResponse enhanceWithProductAndPriceDetails(Order order, CustomerResponse customer) {
        List<OrderItemResponse> items = new ArrayList<>();

        order.getItems().forEach(item -> {
            var product = getProduct(item);
            items.add(mapper.to(item, product));
        });

        return mapper.to(order, customer, items);
    }

    private ProductResponse getProduct(OrderItem item) {
        return productClient.findById(item.getProductId())
                .orElseThrow(() -> new NotFoundException("Product id '%s' not found".formatted(item.getProductId())));
    }
}
