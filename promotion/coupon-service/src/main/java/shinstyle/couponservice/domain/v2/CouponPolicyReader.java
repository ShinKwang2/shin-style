package shinstyle.couponservice.domain.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.exception.CouponPolicyNotFoundException;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.v2.CouponPolicyCacheRepository;
import shinstyle.couponservice.repository.v2.CouponPolicyQuantityRepository;

@RequiredArgsConstructor
@Component
public class CouponPolicyReader {

    private final CouponPolicyCacheRepository couponPolicyCacheRepository;
    private final CouponPolicyRepository couponPolicyRepository;

    public CouponPolicy read(Long couponPolicyId) {
        CouponPolicy cached = couponPolicyCacheRepository.read(couponPolicyId);
        if (cached != null) {
            return cached;
        }
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));
        couponPolicyCacheRepository.add(couponPolicy);
        return couponPolicy;
    }
}
