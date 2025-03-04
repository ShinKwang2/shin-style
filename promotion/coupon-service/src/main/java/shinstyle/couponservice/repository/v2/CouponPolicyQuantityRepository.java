package shinstyle.couponservice.repository.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;
import shinstyle.couponservice.domain.CouponPolicy;

@RequiredArgsConstructor
@Repository
public class CouponPolicyQuantityRepository {

    private final RedissonClient redissonClient;

    private static final String COUPON_QUANTITY_KEY_FORMAT = "coupon::quantity::%s";

    public void add(CouponPolicy couponPolicy) {
        String quantityKey = generateQuantityKey(couponPolicy);
        RAtomicLong atomicQuantity = redissonClient.getAtomicLong(quantityKey);
        atomicQuantity.set(couponPolicy.getTotalQuantity());
    }

    public long decrease(CouponPolicy couponPolicy) {
        String quantityKey = generateQuantityKey(couponPolicy);
        RAtomicLong atomicQuantity = redissonClient.getAtomicLong(quantityKey);
        return atomicQuantity.decrementAndGet();
    }

    public long increase(CouponPolicy couponPolicy) {
        String quantityKey = generateQuantityKey(couponPolicy);
        RAtomicLong atomicQuantity = redissonClient.getAtomicLong(quantityKey);
        return atomicQuantity.incrementAndGet();
    }

    public long read(Long couponPolicyId) {
        String quantityKey = generateQuantityKey(couponPolicyId);
        return redissonClient.getAtomicLong(quantityKey).get();
    }

    private String generateQuantityKey(CouponPolicy couponPolicy) {
        return generateQuantityKey(couponPolicy.getId());
    }

    private String generateQuantityKey(Long policyId) {
        return COUPON_QUANTITY_KEY_FORMAT.formatted(policyId);
    }
}
