package shinstyle.couponservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinstyle.couponservice.exception.CouponAlreadyUsedException;
import shinstyle.couponservice.exception.CouponExpiredException;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "coupons")
@Entity
public class Coupon {

    public enum Status {
        AVAILABLE,
        USED,
        EXPIRED,
        CANCELLED
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coupon_policy_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CouponPolicy couponPolicy;

    private Long userId;
    private String couponCode;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long orderId;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    @Builder
    public Coupon(Long id, CouponPolicy couponPolicy, Long userId, String couponCode) {
        this.id = id;
        this.couponPolicy = couponPolicy;
        this.userId = userId;
        this.couponCode = couponCode;
        this.status = Status.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public void use(Long orderId) {
        if (status == Status.USED) {
            throw new CouponAlreadyUsedException("이미 사용된 쿠폰입니다.");
        }
        if (isExpired()) {
            throw new CouponExpiredException("만료된 쿠폰입니다.");
        }

        this.status = Status.USED;
        this.orderId = orderId;
        this.usedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status != Status.USED) {
            throw new IllegalStateException("사용되지 않은 쿠폰입니다.");
        }

        this.status = Status.CANCELLED;
        this.orderId = null;
        this.usedAt = null;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(couponPolicy.getStartTime()) || now.isAfter(couponPolicy.getEndTime());
    }
}
