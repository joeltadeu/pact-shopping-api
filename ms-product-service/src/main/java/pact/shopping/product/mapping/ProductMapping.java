package pact.shopping.product.mapping;

import org.springframework.stereotype.Component;
import pact.shopping.product.dto.PriceResponse;
import pact.shopping.product.dto.ProductResponse;
import pact.shopping.product.model.Product;

import java.util.List;

@Component
public class ProductMapping {

    public ProductResponse to(Product product, List<PriceResponse> prices) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory().name())
                .quantity(product.getQuantity())
                .prices(prices)
                .build();
    }
}
