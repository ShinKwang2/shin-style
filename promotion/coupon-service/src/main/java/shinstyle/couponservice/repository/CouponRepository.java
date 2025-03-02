package shinstyle.couponservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinstyle.couponservice.domain.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.couponPolicy.id = :policyId")
    Long countByCouponPolicyId(@Param("policyId") Long policyId);

    @Query("SELECT c FROM Coupon c " +
            "WHERE c.userId = :userId AND c.status = :status " +
            "ORDER BY c.createdAt DESC " +
            "LIMIT :limit " +
            "OFFSET :offset")
    List<Coupon> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId,
                                              @Param("status") Coupon.Status status,
                                              @Param("limit") Long limit,
                                              @Param("offset") Long offset);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(Long id);

    List<Coupon> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Coupon.Status status);
}
