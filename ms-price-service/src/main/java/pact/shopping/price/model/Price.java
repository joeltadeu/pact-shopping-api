package pact.shopping.price.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private LocalDateTime createdAt;
    private Long productId;
    private BigDecimal discount;
    private BigDecimal amount;

    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "iso", column = @Column(name = "iso")),
            @AttributeOverride(name = "symbol", column = @Column(name = "symbol")), })
    private PriceCurrency currency;


}
