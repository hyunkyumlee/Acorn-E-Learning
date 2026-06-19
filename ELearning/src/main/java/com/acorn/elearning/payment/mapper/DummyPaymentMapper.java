package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.DummyPayment;
import java.util.List;
import java.util.Optional;

public interface DummyPaymentMapper {
    Optional<DummyPayment> findById(Long id);
    List<DummyPayment> findAll();
    int insert(DummyPayment model);
    int update(DummyPayment model);
}
