package com.acorn.elearning.payment.dto.response;

import com.acorn.elearning.payment.model.PaymentProduct;
import java.math.BigDecimal;
import java.util.List;

public record PaymentProductListResponse(List<Product> products) {
    public static PaymentProductListResponse from(List<PaymentProduct> products) {
        return new PaymentProductListResponse(products.stream()
                .map(Product::from)
                .toList());
    }

    public record Product(
            Long productId,
            String productCode,
            String productName,
            BigDecimal price,
            boolean active
    ) {
        public static Product from(PaymentProduct product) {
            return new Product(
                    product.getProductId(),
                    product.getProductCode(),
                    product.getProductName(),
                    product.getPrice(),
                    Boolean.TRUE.equals(product.getIsActive())
            );
        }
    }
}
