package shinstyle.couponservice.service.v2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.domain.v2.CouponPolicyQuantityManager;
import shinstyle.couponservice.domain.v2.CouponPolicyReader;
import shinstyle.couponservice.domain.v2.coupon.CouponReader;
import shinstyle.couponservice.domain.v2.coupon.CouponWriter;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.exception.CouponIssueException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private CouponPolicyReader couponPolicyReader;

    @Mock
    private CouponWriter couponWriter;

    @Mock
    private CouponReader couponReader;

    @Mock
    private CouponPolicyQuantityManager couponPolicyQuantityManager;

    @Mock
    private RLock rLock;

    @Mock
    private RAtomicLong atomicLong;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_COUPON_ID = 1L;
    private static final Long TEST_POLICY_ID = 1L;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_Success() throws InterruptedException {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        CouponDto.IssueRequest request = CouponDto.IssueRequest.builder()
                .couponPolicyId(TEST_POLICY_ID)
                .build();

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(couponPolicyReader.read(anyLong())).thenReturn(couponPolicy);
        when(couponPolicyQuantityManager.isSoldOut(couponPolicy)).thenReturn(false);
        when(couponWriter.append(any(), anyLong(), anyString(), any())).thenReturn(coupon);


        // When
        CouponDto.Response response = couponService.issueCoupon(request, TEST_USER_ID);

        assertThat(response.getId()).isEqualTo(TEST_COUPON_ID);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        then(couponWriter).should().append(any(), anyLong(), anyString(), any());
        then(rLock).should().unlock();

    }

    @Test
    @DisplayName("쿠폰 발급 실패 - 락 획득 실패")
    void issueCoupon_Fail_LockNotAcquired() throws InterruptedException {
        // Given
        CouponDto.IssueRequest request = CouponDto.IssueRequest.builder()
                .couponPolicyId(TEST_POLICY_ID)
                .build();

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> couponService.issueCoupon(request, TEST_USER_ID))
                .isInstanceOf(CouponIssueException.class)
                .hasMessage("쿠폰 발급 요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("쿠폰 발급 실패 - 수량 소진")
    void issueCoupon_Fail_NoQuantityLeft() throws InterruptedException {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        CouponDto.IssueRequest request = CouponDto.IssueRequest.builder()
                .couponPolicyId(TEST_POLICY_ID)
                .build();

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(couponPolicyReader.read(anyLong())).thenReturn(couponPolicy);
        when(couponPolicyQuantityManager.isSoldOut(couponPolicy)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> couponService.issueCoupon(request, TEST_USER_ID))
                .isInstanceOf(CouponIssueException.class)
                .hasMessage("쿠폰이 모두 소진되었습니다.");
        then(couponPolicyQuantityManager).should(atLeastOnce()).isSoldOut(any());
        then(rLock).should().unlock();
    }



    public static CouponPolicy givenCouponPolicy() {
        return CouponPolicy.builder()
                .id(TEST_POLICY_ID)
                .name("테스트 쿠폰")
                .discountType(CouponPolicy.DiscountType.FIXED_AMOUNT)
                .discountValue(1_000)
                .minimumOrderAmount(10_000)
                .maximumDiscountAmount(1000)
                .totalQuantity(100)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    public static Coupon givenCoupon(CouponPolicy couponPolicy) {
        return Coupon.builder()
                .id(TEST_COUPON_ID)
                .userId(TEST_USER_ID)
                .couponPolicy(couponPolicy)
                .couponCode("TEST123")
                .build();
    }
}