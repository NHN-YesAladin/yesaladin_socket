package shop.yesaladin.socket.coupon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import shop.yesaladin.socket.coupon.service.inter.CouponWebsocketMessageService;

/**
 * 쿠폰 지급 관련 소켓 메시지를 수신하는 컨트롤러 클래스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class CouponSocketController {

    private final CouponWebsocketMessageService websocketMessageSendService;

    /**
     * 소켓 연결 확인 메시지를 수신합니다.
     *
     * @param requestId 연결된 클라이언트의 request id
     */
    @MessageMapping("/coupon/{kind}/result/connect/{requestId}")
    public void receiveConnectMessage(@DestinationVariable String requestId) {
        websocketMessageSendService.registerConnection(requestId);
        log.info("Request id {} socket connected.", requestId);
    }
}
