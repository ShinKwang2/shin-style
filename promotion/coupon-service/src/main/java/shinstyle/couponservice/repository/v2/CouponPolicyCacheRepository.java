package shinstyle.couponservice.repository.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;
import shinstyle.couponservice.domain.CouponPolicy;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CouponPolicyCacheRepository {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String COUPON_POLICY_KEY_FORMAT = "coupon::policy::%s";

    public void add(CouponPolicy couponPolicy) {
        String policyKey = generateKey(couponPolicy.getId());
        try {
            String policyJson = objectMapper.writeValueAsString(couponPolicy);
            RBucket<String> bucket = redissonClient.getBucket(policyKey);
            bucket.set(policyJson);
        } catch (JsonProcessingException e) {
            log.error("쿠폰 정책 정보를 JSON으로 파싱하는 중 오류가 발생했습니다.", e);
        }
    }

    public CouponPolicy read(Long policyId) {
        String policyKey = generateKey(policyId);
        RBucket<String> bucket = redissonClient.getBucket(policyKey);
        String policyJson = bucket.get();
        if (policyJson != null) {
            try {
                return objectMapper.readValue(policyJson, CouponPolicy.class);
            } catch (JsonProcessingException e) {
                log.error("쿠폰 정책 정보를 객체로 파싱하는 중 오류가 발생했습니다.", e);
                return null;
            }
        }
        return null;
    }

    private String generateKey(Long policyId) {
        return COUPON_POLICY_KEY_FORMAT.formatted(policyId);
    }
}
