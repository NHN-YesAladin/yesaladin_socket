package shop.yesaladin.socket.coupon.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import shop.yesaladin.coupon.message.CouponResultDto;
import shop.yesaladin.socket.coupon.domain.model.CouponSocketConnection;
import shop.yesaladin.socket.coupon.domain.repository.CouponResultMessageRepository;
import shop.yesaladin.socket.coupon.domain.repository.CouponSocketConnectionRepository;

/**
 * 생성된지 30분이 지나도 전송되지 않은 메시지 혹은 삭제되지 않은 연결 정보를 1시간에 한 번씩 제거하는 스케쥴러 클래스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
@RequiredArgsConstructor
@Component
public class CouponMessageConnectionClearScheduler {

    private final CouponResultMessageRepository couponResultMessageRepository;
    private final CouponSocketConnectionRepository couponSocketConnectionRepository;
    private final Clock clock;

    /**
     * 연결된지 30분이 지난 쿠폰 연결 정보를 제거합니다.
     *
     * @since 1.0
     */
    @Async
    @Scheduled(fixedDelayString = "PT1H")
    public void clearCouponConnectionData() {
        List<CouponSocketConnection> connectionList = couponSocketConnectionRepository.findAllOver30MinFromConnected(
                LocalDateTime.now(clock));

        connectionList.forEach(connection -> couponResultMessageRepository.deleteByRequestId(
                connection.getRequestId()));
    }

    /**
     * 등록된지 30분이 지난 메시지를 제거합니다.
     *
     * @since 1.0
     */
    @Async
    @Scheduled(fixedDelayString = "PT1H")
    public void clearCouponResultMessage() {
        List<CouponResultDto> messageList = couponResultMessageRepository.findAllOver30MinFromIssued(
                LocalDateTime.now(clock));

        messageList.forEach(result -> couponResultMessageRepository.deleteByRequestId(result.getRequestId()));
    }
}
