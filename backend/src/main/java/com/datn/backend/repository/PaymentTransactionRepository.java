package com.datn.backend.repository;

import com.datn.backend.entity.PaymentTransaction;
import com.datn.backend.entity.enums.PaymentTransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrder_OrderIdOrderByCreatedAtAsc(Long orderId);

    Optional<PaymentTransaction> findFirstByOrder_OrderIdAndProviderAndStatusOrderByCreatedAtDesc(
            Long orderId, String provider, PaymentTransactionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p join fetch p.order where p.paymentId = :paymentId")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("paymentId") Long paymentId);
}
