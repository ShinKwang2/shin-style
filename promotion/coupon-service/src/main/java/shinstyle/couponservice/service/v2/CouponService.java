package shinstyle.couponservice.service.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.domain.v2.CouponPolicyQuantityManager;
import shinstyle.couponservice.domain.v2.CouponPolicyReader;
import shinstyle.couponservice.domain.v2.coupon.CouponReader;
import shinstyle.couponservice.domain.v2.coupon.CouponWriter;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.exception.CouponIssueException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service("couponServiceV2")
public class CouponService {

    private final RedissonClient redissonClient;
    private final CouponPolicyReader couponPolicyReader;
    private final CouponWriter couponWriter;
    private final CouponReader couponReader;
    private final CouponPolicyQuantityManager couponPolicyQuantityManager;

    private static final String COUPON_LOCK_KEY_FORMAT = "coupon::lock::%s";
    private static final long LOCK_WAIT_TIME = 3;
    private static final long LOCK_LEASE_TIME = 5;

    @Transactional
    public CouponDto.Response issueCoupon(CouponDto.IssueRequest request, Long userId) {
        String lockKey = generateLockKey(request.getCouponPolicyId());
        RLock lock = redissonClient.getLock(lockKey);
        

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CouponIssueException("쿠폰 발급 요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }

            CouponPolicy couponPolicy = couponPolicyReader.read(request.getCouponPolicyId());
            if (couponPolicyQuantityManager.isSoldOut(couponPolicy)) {
                throw new CouponIssueException("쿠폰이 모두 소진되었습니다.");
            }
            return CouponDto.Response.from(couponWriter.append(
                    couponPolicy,
                    userId,
                    generateCouponCode(),
                    LocalDateTime.now()
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponIssueException("쿠폰 발급 중 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public CouponDto.Response useCoupon(Long couponId, Long orderId) {
        Coupon coupon = couponWriter.use(couponId, orderId);
        return CouponDto.Response.from(coupon);
    }

    @Transactional
    public CouponDto.Response cancelCoupon(Long couponId) {
        Coupon coupon = couponWriter.cancel(couponId);
        return CouponDto.Response.from(coupon);
    }

    public CouponDto.Response readCoupon(Long couponId) {
        return CouponDto.Response.from(couponReader.read(couponId));
    }

    private String generateCouponCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateLockKey(Long policyId) {
        return COUPON_LOCK_KEY_FORMAT.formatted(policyId);
    }
}
