package shop.yesaladin.socket.coupon.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import shop.yesaladin.coupon.message.CouponResultDto;
import shop.yesaladin.socket.coupon.domain.repository.CouponResultMessageRepository;

/**
 * 쿠폰 지급 결과 메시지를 저장 / 수정 / 삭제하는 레포지토리 인터페이스의 ConcurrentHashMap을 사용한 구현체입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
@RequiredArgsConstructor
@Repository
public class MapCouponResultMessageRepository implements CouponResultMessageRepository {

    private final Map<String, CouponResultDto> couponResultMap = new ConcurrentHashMap<>();

    @Override
    public void save(CouponResultDto result) {
        couponResultMap.put(result.getRequestId(), result);
    }

    @Override
    public boolean existsByRequestId(String requestId) {
        return Objects.nonNull(couponResultMap.get(requestId));
    }

    @Override
    public CouponResultDto getByRequestId(String requestId) {
        return couponResultMap.get(requestId);
    }

    @Override
    public void deleteByRequestId(String requestId) {
        couponResultMap.remove(requestId);
    }

    @Override
    public List<CouponResultDto> findAllOver30MinFromIssued(LocalDateTime now) {
        return couponResultMap.values()
                .stream()
                .filter(result -> now.isAfter(result.getIssuedDateTime().plusMinutes(30)))
                .collect(Collectors.toList());
    }
}
