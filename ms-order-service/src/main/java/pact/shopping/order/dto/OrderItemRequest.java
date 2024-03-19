package pact.shopping.order.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long id;
    private Integer quantity;
}
