package pact.shopping.product.model;

import jakarta.persistence.*;
import lombok.*;
import pact.shopping.product.model.enums.ProductCategoryEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private String name;
    @Enumerated(EnumType.STRING)
    private ProductCategoryEnum category;
    private LocalDateTime createdAt;
    private Integer quantity;
    private Boolean active;
}
