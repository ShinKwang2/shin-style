package shinstyle.couponservice.domain.v2.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shinstyle.common.snowflake.Snowflake;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.exception.CouponNotFoundException;
import shinstyle.couponservice.repository.CouponRepository;
import shinstyle.couponservice.repository.v2.CouponCacheRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class CouponWriter {

    private final Snowflake snowflake = new Snowflake();
    private final CouponRepository couponRepository;
    private final CouponCacheRepository couponCacheRepository;

//    @Transactional
    public Coupon append(CouponPolicy couponPolicy, Long userId, String couponCode, LocalDateTime now) {
        if (now.isBefore(couponPolicy.getStartTime()) || now.isAfter(couponPolicy.getEndTime())) {
            throw new IllegalStateException("쿠폰 발급 기간이 아닙니다.");
        }

        Coupon savedCoupon = couponRepository.save(Coupon.builder()
                .id(snowflake.nextId())
                .userId(userId)
                .couponPolicy(couponPolicy)
                .couponCode(couponCode)
                .build());
        addOrUpdateCache(savedCoupon);

        return savedCoupon;
    }

    public Coupon use(Long couponId, Long orderId) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));
        coupon.use(orderId);
        addOrUpdateCache(coupon);
        return couponRepository.save(coupon);
    }

    public Coupon cancel(Long couponId) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));

        coupon.cancel();
        addOrUpdateCache(coupon);
        return couponRepository.save(coupon);
    }

    private void addOrUpdateCache(Coupon savedCoupon) {
        couponCacheRepository.addOrUpdate(savedCoupon);
    }
}
