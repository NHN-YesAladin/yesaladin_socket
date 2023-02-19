package shop.yesaladin.socket.coupon.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import shop.yesaladin.socket.coupon.domain.model.CouponSocketConnection;

/**
 * 쿠폰 지급 관련 소켓 연결 정보를 저장 / 조회 / 삭제하는 레포지토리 인터페이스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
public interface CouponSocketConnectionRepository {

    void save(CouponSocketConnection connection);

    boolean existsByRequestId(String requestId);

    void deleteByRequestId(String requestId);

    List<CouponSocketConnection> findAllOver30MinFromConnected(LocalDateTime now);

}
