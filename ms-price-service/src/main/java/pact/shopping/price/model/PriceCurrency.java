package pact.shopping.price.model;


import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PriceCurrency {
    private String symbol;
    @Enumerated(EnumType.STRING)
    private CurrencyEnum iso;
}
