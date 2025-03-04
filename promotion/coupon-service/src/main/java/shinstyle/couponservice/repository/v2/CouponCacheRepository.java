package shinstyle.couponservice.repository.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import shinstyle.couponservice.domain.Coupon;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponCacheRepository {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String COUPON_STATE_KEY_FORMAT = "coupon::state::$s";

    /**
     * 쿠폰 상태를 Redis에 저장
     * @Param coupon 상태를 저장할 쿠폰
     */
    public void addOrUpdate(Coupon coupon) {
        try {
            String couponStateKey = generateKey(coupon);
            String couponJson = objectMapper.writeValueAsString(coupon);
            RBucket<Object> bucket = redissonClient.getBucket(couponStateKey);
            bucket.set(couponJson);

            log.info("Coupon state updated: {}::{}", coupon.getId(), coupon.getStatus());
        } catch (Exception e) {
            log.error("Error updating coupon state: {}", e.getMessage(), e);
            throw new RuntimeException("쿠폰 상태 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    public Coupon read(Long couponId) {
        try {
            String couponStateKey = generateKey(couponId);
            RBucket<String> bucket = redissonClient.getBucket(couponStateKey);
            String couponJson = bucket.get();
            if (couponJson == null) {
                return null;
            }
            return objectMapper.readValue(couponJson, Coupon.class);
        } catch (Exception e) {
            log.error("Error getting coupon state: {}", e.getMessage(), e);
            throw new RuntimeException("쿠폰 상태 조회 중 오류가 발생했습니다.", e);
        }
    }

    private String generateKey(Coupon coupon) {
        return generateKey(coupon.getId());
    }

    private String generateKey(Long couponId) {
        return COUPON_STATE_KEY_FORMAT.formatted(couponId);
    }
}
