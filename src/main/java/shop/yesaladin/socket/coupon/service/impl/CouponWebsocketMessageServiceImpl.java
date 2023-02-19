package shop.yesaladin.socket.coupon.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import shop.yesaladin.coupon.code.CouponSocketRequestKind;
import shop.yesaladin.coupon.message.CouponResultDto;
import shop.yesaladin.socket.config.SocketProperties;
import shop.yesaladin.socket.coupon.domain.model.CouponSocketConnection;
import shop.yesaladin.socket.coupon.domain.repository.CouponResultMessageRepository;
import shop.yesaladin.socket.coupon.domain.repository.CouponSocketConnectionRepository;
import shop.yesaladin.socket.coupon.service.inter.CouponWebsocketMessageService;

/**
 * 웹소켓을 사용하여 쿠폰 관련 메시지를 발송하는 클래스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
@RequiredArgsConstructor
@Component
public class CouponWebsocketMessageServiceImpl implements CouponWebsocketMessageService {

    private final CouponResultMessageRepository couponResultMessageRepository;
    private final CouponSocketConnectionRepository couponSocketConnectionRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SocketProperties socketProperties;
    private final Clock clock;

    /**
     * {@inheritDoc}
     */
    @Override
    public void trySendGiveCouponResultMessage(CouponResultDto resultDto) {
        if (canSendMessage(resultDto)) {
            sendMessage(resultDto);
            clearConnectionAndMessage(resultDto);
            return;
        }
        couponResultMessageRepository.save(resultDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerConnection(String requestId) {
        CouponSocketConnection connection = new CouponSocketConnection(
                requestId,
                LocalDateTime.now(clock)
        );

        couponSocketConnectionRepository.save(connection);

        if (couponResultMessageRepository.existsByRequestId(requestId)) {
            CouponResultDto result = couponResultMessageRepository.getByRequestId(requestId);
            trySendGiveCouponResultMessage(result);
        }
    }

    private boolean canSendMessage(CouponResultDto resultDto) {
        return couponSocketConnectionRepository.existsByRequestId(resultDto.getRequestId());
    }

    private void sendMessage(CouponResultDto result) {
        String topicPrefix = result.getRequestKind().equals(CouponSocketRequestKind.GIVE)
                ? socketProperties.getCouponGiveResultTopicPrefix()
                : socketProperties.getCouponUseResultTopicPrefix();

        messagingTemplate.convertAndSend(
                topicPrefix + result.getRequestId(),
                result
        );
    }

    private void clearConnectionAndMessage(CouponResultDto resultDto) {
        couponSocketConnectionRepository.deleteByRequestId(resultDto.getRequestId());
        couponResultMessageRepository.deleteByRequestId(resultDto.getRequestId());
    }
}
