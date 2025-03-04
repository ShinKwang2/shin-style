package shinstyle.couponservice.service.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinstyle.common.snowflake.Snowflake;
import shinstyle.couponservice.domain.CouponPolicy;
import shinstyle.couponservice.domain.v2.CouponPolicyReader;
import shinstyle.couponservice.dto.v1.CouponPolicyDto;
import shinstyle.couponservice.exception.CouponPolicyNotFoundException;
import shinstyle.couponservice.repository.CouponPolicyRepository;
import shinstyle.couponservice.repository.v2.CouponPolicyCacheRepository;
import shinstyle.couponservice.repository.v2.CouponPolicyQuantityRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("couponPolicyServiceV2")
public class CouponPolicyService {

    private final Snowflake snowflake = new Snowflake();
    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponPolicyCacheRepository couponPolicyCacheRepository;
    private final CouponPolicyQuantityRepository couponPolicyQuantityRepository;
    private final CouponPolicyReader couponPolicyReader;

    @Transactional
    public CouponPolicyDto.Response create(CouponPolicyDto.CreateRequest request) throws JsonProcessingException {
        CouponPolicy savedPolicy = couponPolicyRepository.save(
                request.toEntity(snowflake.nextId())
        );

        // Redis에 초기 수량 설정
        couponPolicyQuantityRepository.add(savedPolicy);

        // Redis에 정책 정보 저장
        couponPolicyCacheRepository.add(savedPolicy);

        return CouponPolicyDto.Response.from(savedPolicy);
    }

    public CouponPolicyDto.Response read(Long id) {
        return CouponPolicyDto.Response.from(couponPolicyReader.read(id));
    }

    public List<CouponPolicyDto.Response> readAll() {
        return couponPolicyRepository.findAll().stream()
                .map(CouponPolicyDto.Response::from)
                .toList();
    }
}
