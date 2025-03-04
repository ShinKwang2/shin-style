package shinstyle.couponservice.service.v2;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.dto.v1.CouponPolicyDto;
import shinstyle.couponservice.exception.CouponIssueException;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.CouponRepository;
import shinstyle.couponservice.repository.v2.CouponPolicyQuantityRepository;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(properties = "eureka.client.enabled=false")
public class CouponServiceConcurrencyTestV2 {

    @Autowired
    @Qualifier("couponServiceV2")
    CouponService couponService;

    @Autowired
    @Qualifier("couponPolicyServiceV2")
    CouponPolicyService couponPolicyService;

    @Autowired
    CouponPolicyRepository couponPolicyRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponPolicyQuantityRepository quantityRepository;

    @Test
    void 쿠폰_신청_동시성_테스트V2() throws Exception {
        // Given
        CouponPolicyDto.CreateRequest createRequest = givenCouponPolicyDto(10);
        CouponPolicyDto.Response response = couponPolicyService.create(createRequest);

        int theadCount = 100;
        CountDownLatch latch = new CountDownLatch(theadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < theadCount; i++) {
            Long userId = Long.valueOf(i);
            executorService.execute(() -> {
                try {
                couponService.issueCoupon(CouponDto.IssueRequest.builder()
                        .couponPolicyId(response.getId())
                        .build(), userId);
                } catch (CouponIssueException e) {
                    log.info("쿠폰 발급 실패: {} - {}", userId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Assertions.assertThat(couponRepository.countByCouponPolicyId(response.getId())).isEqualTo(10);
        Assertions.assertThat(quantityRepository.read(response.getId())).isEqualTo(0);
    }

    private CouponPolicyDto.CreateRequest givenCouponPolicyDto(Integer totalQuantity) {
        return CouponPolicyDto.CreateRequest.builder()
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
