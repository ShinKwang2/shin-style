package shinstyle.couponservice.service.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import shinstyle.couponservice.config.UserIdInterceptor;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.exception.CouponIssueException;
import shinstyle.couponservice.exception.CouponNotFoundException;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.CouponRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponPolicyRepository couponPolicyRepository;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_COUPON_ID = 1L;
    private static final Long TEST_ORDER_ID = 1L;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_Success() {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Coupon coupon = givenCoupon(couponPolicy);
        CouponDto.IssueRequest request = CouponDto.IssueRequest.builder()
                .couponPolicyId(1L)
                .build();

        BDDMockito.when(couponPolicyRepository.findByIdWithLock(BDDMockito.any())).thenReturn(Optional.of(couponPolicy));
        BDDMockito.when(couponRepository.countByCouponPolicyId(BDDMockito.any())).thenReturn(0L);
        BDDMockito.when(couponRepository.save(BDDMockito.any())).thenReturn(coupon);

        try (MockedStatic<UserIdInterceptor> mockedStatic = BDDMockito.mockStatic(UserIdInterceptor.class)) {
            mockedStatic.when(UserIdInterceptor::getCurrentUserId).thenReturn(TEST_USER_ID);

            // When
            CouponDto.Response response = couponService.issueCoupon(request);

            // Then
            assertThat(response.getId()).isEqualTo(TEST_COUPON_ID);
            assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
            BDDMockito.then(couponRepository).should(BDDMockito.atLeastOnce()).save(BDDMockito.any(Coupon.class));
        }
    }

    @ParameterizedTest
    @DisplayName("쿠폰 발급 실패 - 시작 시간 이전 / 종료 시간 이후")
    @MethodSource("timeParamsForIssueCoupon")
    void issueCoupon_Fail_CouponIssuedException_Time(LocalDateTime starTime, LocalDateTime endTime) {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy(starTime, endTime);
        CouponDto.IssueRequest request = CouponDto.IssueRequest.builder()
                .couponPolicyId(1L)
                .build();

        BDDMockito.when(couponPolicyRepository.findByIdWithLock(BDDMockito.any())).thenReturn(Optional.of(couponPolicy));

        // When & Then
        assertThatThrownBy(() -> couponService.issueCoupon(request))
                .isInstanceOf(CouponIssueException.class)
                .hasMessageContaining("쿠폰 발급 기간이 아닙니다.");
    }

    private static Stream<Arguments> timeParamsForIssueCoupon() {
        return Stream.of(
                Arguments.of(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3)),
                Arguments.of(LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1))
        );
    }

    @Test
    @DisplayName("쿠폰 사용 성공")
    void useCoupon_Success() {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        BDDMockito.when(couponRepository.findByIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                .thenReturn(Optional.of(coupon));

        try (MockedStatic<UserIdInterceptor> mockedStatic = BDDMockito.mockStatic(UserIdInterceptor.class)) {
            mockedStatic.when(UserIdInterceptor::getCurrentUserId).thenReturn(TEST_USER_ID);

            // When
            CouponDto.Response response = couponService.useCoupon(TEST_COUPON_ID, TEST_ORDER_ID);

            // Then
            assertThat(response.getId()).isEqualTo(TEST_COUPON_ID);
            assertThat(response.getOrderId()).isEqualTo(TEST_ORDER_ID);
            assertThat(response.getStatus()).isEqualTo(Coupon.Status.USED);
        }
    }

    @Test
    @DisplayName("쿠폰 사용 실패 - 쿠폰이 존재하지 않거나 권한 없음")
    void useCoupon_Fail_NotFoundOrUnauthorized() {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        BDDMockito.when(couponRepository.findByIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());

        try (MockedStatic<UserIdInterceptor> mockedStatic = BDDMockito.mockStatic(UserIdInterceptor.class)) {
            mockedStatic.when(UserIdInterceptor::getCurrentUserId).thenReturn(TEST_USER_ID);

            // When & Then
            assertThatThrownBy(() -> couponService.useCoupon(TEST_COUPON_ID, TEST_ORDER_ID))
                    .isInstanceOf(CouponNotFoundException.class)
                    .hasMessage("쿠폰을 찾을 수 없거나 접근 권한이 없습니다.");
        }
    }

    @Test
    @DisplayName("쿠폰 취소 성공")
    void cancelCoupon_Success() {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        coupon.use(TEST_ORDER_ID);
        BDDMockito.when(couponRepository.findByIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                .thenReturn(Optional.of(coupon));

        try (MockedStatic<UserIdInterceptor> mockedStatic = BDDMockito.mockStatic(UserIdInterceptor.class)) {
            mockedStatic.when(UserIdInterceptor::getCurrentUserId).thenReturn(TEST_USER_ID);

            // When
            CouponDto.Response response = couponService.cancelCoupon(TEST_COUPON_ID);

            // Then
            assertThat(response.getId()).isEqualTo(TEST_COUPON_ID);
            assertThat(response.getStatus()).isEqualTo(Coupon.Status.CANCELLED);
        }
    }

    @Test
    @DisplayName("쿠폰 취소 실패 - 쿠폰이 존재하지 않거나 권한 없음")
    void cancelCoupon_Fail_NotFoundOrUnauthorized() {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        coupon.use(TEST_ORDER_ID);
        BDDMockito.when(couponRepository.findByIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());

        try (MockedStatic<UserIdInterceptor> mockedStatic = BDDMockito.mockStatic(UserIdInterceptor.class)) {
            mockedStatic.when(UserIdInterceptor::getCurrentUserId).thenReturn(TEST_USER_ID);

            // When & Then
            assertThatThrownBy(() -> couponService.cancelCoupon(TEST_COUPON_ID))
                    .isInstanceOf(CouponNotFoundException.class)
                    .hasMessage("쿠폰을 찾을 수 없거나 접근 권한이 없습니다.");
        }
    }
    @Test
    @DisplayName("쿠폰 취소 실패 - 쿠폰을 아직 사용하지 않음")
    void cancelCoupon_Fail_NotUsed() {
        // Given
        CouponPolicy couponPolicy = givenCouponPolicy();
        Coupon coupon = givenCoupon(couponPolicy);
        BDDMockito.when(couponRepository.findByIdAndUserId(TEST_COUPON_ID, TEST_USER_ID))
                .thenReturn(Optional.of(coupon));

        try (MockedStatic<UserIdInterceptor> mockedStatic = BDDMockito.mockStatic(UserIdInterceptor.class)) {
            mockedStatic.when(UserIdInterceptor::getCurrentUserId).thenReturn(TEST_USER_ID);

            // When & Then
            assertThatThrownBy(() -> couponService.cancelCoupon(TEST_COUPON_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("사용되지 않은 쿠폰입니다.");
        }
    }

    private CouponPolicy givenCouponPolicy() {
        return givenCouponPolicy(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    }
    private CouponPolicy givenCouponPolicy(LocalDateTime startTime, LocalDateTime endTime) {
        return CouponPolicy.builder()
                .id(1L)
                .name("테스트 쿠폰")
                .discountType(CouponPolicy.DiscountType.FIXED_AMOUNT)
                .discountValue(1_000)
                .minimumOrderAmount(10_000)
                .maximumDiscountAmount(1000)
                .totalQuantity(100)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    private Coupon givenCoupon(CouponPolicy couponPolicy) {
        return Coupon.builder()
                .id(TEST_COUPON_ID)
                .userId(TEST_USER_ID)
                .couponPolicy(couponPolicy)
                .couponCode("TEST123")
                .build();
    }
}