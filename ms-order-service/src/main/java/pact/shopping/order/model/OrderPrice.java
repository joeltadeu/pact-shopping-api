package pact.shopping.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class OrderPrice {
    private BigDecimal amount;
    private BigDecimal discount;

    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "iso", column = @Column(name = "iso")),
            @AttributeOverride(name = "symbol", column = @Column(name = "symbol")), })
    private PriceCurrency currency;


}
