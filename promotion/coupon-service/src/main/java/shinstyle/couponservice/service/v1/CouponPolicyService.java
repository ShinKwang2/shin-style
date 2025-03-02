package shinstyle.couponservice.service.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinstyle.common.snowflake.Snowflake;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.dto.v1.CouponPolicyDto;
import shinstyle.couponservice.exception.CouponPolicyNotFoundException;
import shinstyle.couponservice.repository.CouponPolicyRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponPolicyService {

    private final Snowflake snowflake = new Snowflake();
    private final CouponPolicyRepository couponPolicyRepository;

    public CouponPolicyDto.Response create(CouponPolicyDto.CreateRequest request) {
        CouponPolicy savedCouponPolicy = couponPolicyRepository.save(request.toEntity(snowflake.nextId()));
        return CouponPolicyDto.Response.from(savedCouponPolicy);
    }

    public CouponPolicyDto.Response read(Long id) {
        CouponPolicy couponPolicy = couponPolicyRepository.findById(id)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));

        return CouponPolicyDto.Response.from(couponPolicy);
    }

    public List<CouponPolicyDto.Response> readAll() {
        return couponPolicyRepository.findAll().stream()
                .map(CouponPolicyDto.Response::from)
                .toList();
    }
}
