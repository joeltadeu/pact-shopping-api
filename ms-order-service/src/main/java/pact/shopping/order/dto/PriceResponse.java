package pact.shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceResponse {
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal total;
    private CurrencyDto currency;
}
