package shinstyle.couponservice.domain.v2.coupon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.CouponRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(properties = "eureka.client.enabled=false")
class CouponWriterTest {

    @Autowired
    private CouponWriter couponWriter;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    private static final Long TEST_POLICY_ID = 2L;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_COUPON_CODE = "test-code";

    @AfterEach
    void tearDown() {
        couponRepository.deleteAllInBatch();
        couponPolicyRepository.deleteAllInBatch();
    }

    @Test
    void couponAppend_Success() {
        // Given
        CouponPolicy savedPolicy = couponPolicyRepository.save(givenCouponPolicy());

        // When
        Coupon savedCoupon = couponWriter.append(savedPolicy, TEST_USER_ID, TEST_COUPON_CODE, LocalDateTime.now());

        // Then
        assertThat(savedCoupon).isNotNull();
        assertThat(savedCoupon.getCouponPolicy().getId()).isEqualTo(TEST_POLICY_ID);
        assertThat(savedCoupon.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedCoupon.getCouponCode()).isEqualTo(TEST_COUPON_CODE);
    }

    @Test
    void couponAppend_Fail_InvalidPeriod() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        CouponPolicy savedPolicy = couponPolicyRepository.save(
                givenCouponPolicy(
                        now.plusDays(1),
                        now.plusDays(3)
                )
        );

        // When & Then
        assertThatThrownBy(() -> couponWriter.append(savedPolicy, TEST_USER_ID, TEST_COUPON_CODE, now))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("쿠폰 발급 기간이 아닙니다.");

    }

    private CouponPolicy givenCouponPolicy() {
        return givenCouponPolicy(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    }

    private CouponPolicy givenCouponPolicy(LocalDateTime startDate, LocalDateTime endDate) {
        return CouponPolicy.builder()
                .id(TEST_POLICY_ID)
                .name("테스트 쿠폰")
                .discountType(CouponPolicy.DiscountType.FIXED_AMOUNT)
                .discountValue(1_000)
                .minimumOrderAmount(10_000)
                .maximumDiscountAmount(1000)
                .totalQuantity(100)
                .startTime(startDate)
                .endTime(endDate)
                .build();
    }
}