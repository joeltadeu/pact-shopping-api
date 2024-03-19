package pact.shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private Integer quantity;
    private List<PriceResponse> prices;

    public PriceResponse getCurrentPrice() {
        var price = Optional.ofNullable(prices)
                .orElseGet(Collections::emptyList)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product doesnt have price"));
        price.setTotal(price.getAmount().multiply(BigDecimal.valueOf(quantity)));
        return price;
    }
}
