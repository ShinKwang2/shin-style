package shinstyle.couponservice.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinstyle.couponservice.dto.v1.CouponPolicyDto;
import shinstyle.couponservice.service.v1.CouponPolicyService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/coupon-policies")
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;

    @PostMapping
    public ResponseEntity<CouponPolicyDto.Response> create(
            @RequestBody CouponPolicyDto.CreateRequest request
    ) {
        return ResponseEntity.ok(couponPolicyService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponPolicyDto.Response> read(@PathVariable Long id) {
        return ResponseEntity.ok(couponPolicyService.read(id));
    }

    @GetMapping
    public ResponseEntity<List<CouponPolicyDto.Response>> readAll() {
        return ResponseEntity.ok(couponPolicyService.readAll());
    }
}
