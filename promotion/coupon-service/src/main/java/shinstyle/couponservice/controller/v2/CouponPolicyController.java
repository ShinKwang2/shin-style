package shinstyle.couponservice.controller.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinstyle.couponservice.dto.v1.CouponPolicyDto;
import shinstyle.couponservice.service.v2.CouponPolicyService;

import java.util.List;

@RequiredArgsConstructor
@RestController("couponPolicyControllerV2")
@RequestMapping("/ap1/v2/coupon-policies")
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;

    @PostMapping
    public ResponseEntity<CouponPolicyDto.Response> create(
            @RequestBody CouponPolicyDto.CreateRequest request
    ) throws JsonProcessingException {
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
