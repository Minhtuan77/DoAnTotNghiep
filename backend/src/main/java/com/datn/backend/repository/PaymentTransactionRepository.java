package com.datn.backend.repository;

import com.datn.backend.entity.PaymentTransaction;
import com.datn.backend.entity.enums.PaymentTransactionStatus;
import com.datn.backend.entity.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrder_OrderIdOrderByCreatedAtAsc(Long orderId);

    Optional<PaymentTransaction> findFirstByOrder_OrderIdAndProviderAndStatusOrderByCreatedAtDesc(
            Long orderId, String provider, PaymentTransactionStatus status);

    @Query("""
            select p.paymentId from PaymentTransaction p
            where upper(p.provider) = 'VNPAY'
              and p.order.status = :orderStatus
              and p.createdAt < :cutoff
              and p.createdAt = (
                  select max(p2.createdAt) from PaymentTransaction p2
                  where p2.order.orderId = p.order.orderId
                    and upper(p2.provider) = 'VNPAY'
              )
            """)
    List<Long> findExpiredLatestVnpayPaymentIds(@Param("cutoff") LocalDateTime cutoff, @Param("orderStatus") OrderStatus orderStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p join fetch p.order where p.paymentId = :paymentId")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("paymentId") Long paymentId);
}
