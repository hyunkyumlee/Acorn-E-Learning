package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.PaymentProduct;
import java.util.List;
import java.util.Optional;

public interface PaymentProductMapper {
    Optional<PaymentProduct> findById(Long id);
    List<PaymentProduct> findAll();
    int insert(PaymentProduct model);
    int update(PaymentProduct model);
}
