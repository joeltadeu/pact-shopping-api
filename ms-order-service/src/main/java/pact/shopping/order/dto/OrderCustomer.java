package pact.shopping.order.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCustomer {
    private Long id;
    private String firstName;
    private String lastName;
}
