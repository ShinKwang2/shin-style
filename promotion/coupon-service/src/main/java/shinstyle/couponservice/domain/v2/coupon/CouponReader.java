package shinstyle.couponservice.domain.v2.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.exception.CouponNotFoundException;
import shinstyle.couponservice.repository.CouponRepository;
import shinstyle.couponservice.repository.v2.CouponCacheRepository;

@RequiredArgsConstructor
@Component
public class CouponReader {

    private final CouponRepository couponRepository;
    private final CouponCacheRepository couponCacheRepository;

    public Coupon read(Long couponId) {
        Coupon cachedCoupon = couponCacheRepository.read(couponId);
        if (cachedCoupon != null) {
            return cachedCoupon;
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));
        couponCacheRepository.addOrUpdate(coupon);
        return coupon;
    }
}
