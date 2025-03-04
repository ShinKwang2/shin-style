package shinstyle.couponservice.service.v1;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.exception.CouponIssueException;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.CouponRepository;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(properties = "eureka.client.enabled=false")
public class CouponServiceConcurrencyTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponPolicyRepository couponPolicyRepository;

    @Test
    void 쿠폰_신청_동시성_테스트() throws Exception {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy(100);
        CouponPolicy savedCouponPolicy = couponPolicyRepository.save(couponPolicy);

        int threadCount = 300;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        for (long i = 0; i < threadCount; i++) {
            Long userId = Long.valueOf(i);
            executorService.execute(() -> {
                try {
                    couponService.issueCoupon(CouponDto.IssueRequest.builder()
                            .couponPolicyId(savedCouponPolicy.getId())
                            .build(), userId);
                } catch (CouponIssueException e) {
                    log.info("쿠폰 발급 실패: {}", userId);
                } finally {
                    latch.countDown();
                }

            });
        }
        latch.await();

        assertThat(couponRepository.countByCouponPolicyId(savedCouponPolicy.getId())).isEqualTo(100);
    }

    private CouponPolicy givenCouponPolicy(Integer totalQuantity) {
        return CouponPolicy.builder()
                .id(1L)
                .name("테스트 쿠폰")
                .discountType(CouponPolicy.DiscountType.FIXED_AMOUNT)
                .discountValue(1_000)
                .minimumOrderAmount(10_000)
                .maximumDiscountAmount(1000)
                .totalQuantity(totalQuantity)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .build();
    }
}

