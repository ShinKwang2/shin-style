package shinstyle.couponservice.service.v1;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.exception.CouponIssueException;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.CouponRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static shinstyle.couponservice.service.v1.CouponServiceTest.givenCouponPolicy;

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
        CouponPolicy couponPolicy = givenCouponPolicy();
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
}

