package shinstyle.couponservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import shinstyle.couponservice.domain.CouponPolicy;

import java.util.Optional;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cp FROM CouponPolicy cp WHERE cp.id = :id")
    Optional<CouponPolicy> findByIdWithLock(Long id);
}
