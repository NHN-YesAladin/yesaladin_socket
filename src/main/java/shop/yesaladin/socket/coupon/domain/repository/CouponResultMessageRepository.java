package shop.yesaladin.socket.coupon.domain.repository;


import java.time.LocalDateTime;
import java.util.List;
import shop.yesaladin.coupon.message.CouponResultDto;

/**
 * 쿠폰 지급 결과 메시지를 저장 / 수정 / 삭제하는 레포지토리 인터페이스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
public interface CouponResultMessageRepository {

    void save(CouponResultDto result);

    boolean existsByRequestId(String requestId);

    CouponResultDto getByRequestId(String requestId);

    void deleteByRequestId(String requestId);

    List<CouponResultDto> findAllOver30MinFromIssued(LocalDateTime now);
}
