package pact.shopping.order.mapping;

import org.springframework.stereotype.Component;
import pact.shopping.order.dto.*;
import pact.shopping.order.model.Order;
import pact.shopping.order.model.OrderItem;
import pact.shopping.order.model.OrderPrice;
import pact.shopping.order.model.PriceCurrency;
import pact.shopping.order.model.enums.OrderStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OrderMapping {
    public Order to(OrderRequest request, Long customerId) {
        List<OrderItem> items = new ArrayList<>();
        var order = Order.builder()
                .customerId(customerId)
                .uuid(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .status(OrderStatusEnum.DONE)
                .items(items)
                .build();

        request.getItems().forEach(item -> {
            items.add(OrderItem.builder()
                    .order(order)
                    .productId(item.getId())
                    .quantity(item.getQuantity())
                    .build());
        });

        return order;
    }

    public OrderResponse to(Order order, CustomerResponse customer, List<OrderItemResponse> items) {
        var total = items.stream().map(OrderItemResponse::getPrice).map(PriceResponse::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return OrderResponse.builder()
                .id(order.getId())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .customer(OrderCustomer.builder()
                        .id(customer.getId())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .build())
                .items(items)
                .total(total)
                .build();
    }

    public OrderItemResponse to(OrderItem item, ProductResponse product) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .name(product.getName())
                .quantity(item.getQuantity())
                .price(product.getCurrentPrice())
                .build();
    }

    public OrderPrice to(PriceResponse price) {
        return OrderPrice.builder()
                .amount(price.getAmount())
                .discount(price.getDiscount())
                .currency(PriceCurrency.builder()
                        .symbol(price.getCurrency().getSymbol())
                        .iso(price.getCurrency().getIso())
                        .build())
                .build();
    }
}
