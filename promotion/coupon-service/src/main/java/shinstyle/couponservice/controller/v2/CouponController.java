package shinstyle.couponservice.controller.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinstyle.couponservice.config.UserIdInterceptor;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.service.v2.CouponService;

@RequiredArgsConstructor
@RestController("couponControllerV2")
@RequestMapping("/api/v2/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<CouponDto.Response> issueCoupon(@RequestBody CouponDto.IssueRequest request) {
        Long userId = UserIdInterceptor.getCurrentUserId();
        return ResponseEntity.ok(couponService.issueCoupon(request, userId));
    }

    @PostMapping("/{couponId}/use")
    public ResponseEntity<CouponDto.Response> useCoupon(
            @PathVariable Long couponId,
            @RequestBody CouponDto.UseRequest request
    ) {
        return ResponseEntity.ok(couponService.useCoupon(couponId, request.getOrderId()));
    }

    @PostMapping("/{couponId}/cancel")
    public ResponseEntity<CouponDto.Response> cancelCoupon(@PathVariable Long couponId) {
        return ResponseEntity.ok(couponService.cancelCoupon(couponId));
    }
}
