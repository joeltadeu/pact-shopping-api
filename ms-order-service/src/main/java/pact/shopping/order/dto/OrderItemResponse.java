package pact.shopping.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private String name;
    private Integer quantity;
    private PriceResponse price;
}
