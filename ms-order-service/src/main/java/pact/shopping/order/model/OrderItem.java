package pact.shopping.order.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"order"})
@Table(name = "ordered_item")
public class OrderItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private Order order;
    private Long productId;
    private Integer quantity;

    @Embedded
    @AttributeOverrides({ @AttributeOverride(name = "amount", column = @Column(name = "amount")),
            @AttributeOverride(name = "discount", column = @Column(name = "discount")), })
    private OrderPrice price;
}
