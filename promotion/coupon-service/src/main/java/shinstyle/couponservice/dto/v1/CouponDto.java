package shinstyle.couponservice.dto.v1;

import lombok.*;
import shinstyle.couponservice.domain.Coupon;
import shinstyle.couponservice.domain.CouponPolicy;

import java.time.LocalDateTime;

public class CouponDto {

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class IssueRequest {
        private Long couponPolicyId;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UseRequest {
        private Long orderId;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ListRequest {

        private static final Long MAX_PAGE_SIZE = 100L;

        private Coupon.Status status;
        private Long page;
        private Long pageSize;

        public Long getOffset() {
            return (Math.max(1L, page) - 1) * getLimit();
        }

        public Long getLimit() {
            return Math.min(pageSize, MAX_PAGE_SIZE);
        }
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String couponCode;
        private CouponPolicy.DiscountType discountType;
        private int discountValue;
        private int minimumOrderAmount;
        private int maximumDiscountAmount;
        private LocalDateTime validFrom;
        private LocalDateTime validUntil;
        private Coupon.Status status;
        private Long orderId;
        private LocalDateTime usedAt;

        public static Response from(Coupon coupon) {
            CouponPolicy policy = coupon.getCouponPolicy();
            return Response.builder()
                    .id(coupon.getId())
                    .userId(coupon.getUserId())
                    .couponCode(coupon.getCouponCode())
                    .discountType(policy.getDiscountType())
                    .discountValue(policy.getDiscountValue())
                    .minimumOrderAmount(policy.getMinimumOrderAmount())
                    .maximumDiscountAmount(policy.getMaximumDiscountAmount())
                    .validFrom(policy.getStartTime())
                    .validUntil(policy.getEndTime())
                    .status(coupon.getStatus())
                    .orderId(coupon.getOrderId())
                    .usedAt(coupon.getUsedAt())
                    .build();
        }
    }
}
