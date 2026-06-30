package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.PaymentProduct;
import java.util.List;
import java.util.Optional;

public interface PaymentProductMapper {
    Optional<PaymentProduct> findById(Long id);
    Optional<PaymentProduct> findByCode(String productCode);
    List<PaymentProduct> findAll();
    List<PaymentProduct> findActiveProducts();
    int insert(PaymentProduct model);
    int update(PaymentProduct model);
}
