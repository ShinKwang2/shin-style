package shinstyle.couponservice.domain.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.repository.v2.CouponPolicyQuantityRepository;

@RequiredArgsConstructor
@Component
public class CouponPolicyQuantityManager {

    private final CouponPolicyQuantityRepository couponPolicyQuantityRepository;

    public boolean isSoldOut(CouponPolicy couponPolicy) {
        long remainingQuantity = couponPolicyQuantityRepository.decrease(couponPolicy);
        if (remainingQuantity < 0) {
            couponPolicyQuantityRepository.increase(couponPolicy);
            return true;
        }
        return false;
    }
}
