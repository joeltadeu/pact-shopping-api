package pact.shopping.order.dto;

import lombok.Builder;
import lombok.Data;
import pact.shopping.order.model.enums.OrderStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private LocalDateTime createdAt;
    private OrderStatusEnum status;
    private OrderCustomer customer;
    private List<OrderItemResponse> items;
    private BigDecimal total;
}
