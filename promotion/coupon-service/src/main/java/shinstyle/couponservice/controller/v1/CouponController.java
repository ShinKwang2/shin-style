package shinstyle.couponservice.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinstyle.couponservice.dto.v1.CouponDto;
import shinstyle.couponservice.service.v1.CouponService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<CouponDto.Response> issueCoupon(@RequestBody CouponDto.IssueRequest request) {
        return ResponseEntity.ok(couponService.issueCoupon(request));
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

    @GetMapping
    public ResponseEntity<List<CouponDto.Response>> readAll(@ModelAttribute CouponDto.ListRequest request) {
        return ResponseEntity.ok(couponService.readAll(request));
    }
}
