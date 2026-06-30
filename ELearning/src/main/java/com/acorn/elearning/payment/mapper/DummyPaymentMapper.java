package com.acorn.elearning.payment.mapper;

import com.acorn.elearning.payment.model.DummyPayment;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface DummyPaymentMapper {
    Optional<DummyPayment> findById(Long id);
    Optional<DummyPayment> findByOrderNo(String orderNo);
    Optional<DummyPayment> findByIdAndUserId(@Param("paymentId") Long paymentId, @Param("userId") Long userId);
    List<DummyPayment> findAll();
    List<DummyPayment> findByUserId(Long userId);
    Optional<DummyPayment> findLatestByUserId(Long userId);
    int insert(DummyPayment model);
    int insertPaid(DummyPayment model);
    int update(DummyPayment model);
}
