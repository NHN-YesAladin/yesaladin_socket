package shop.yesaladin.socket.coupon.service.inter;


import shop.yesaladin.coupon.message.CouponResultDto;

/**
 * 웹소켓을 사용하여 쿠폰 관련 메시지를 발송하는 인터페이스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
public interface CouponWebsocketMessageService {


    /**
     * 쿠폰 지급 결과 메시지를 클라이언트로 전송합니다.
     *
     * @param resultDto 전송할 메시지
     */
    void trySendGiveCouponResultMessage(CouponResultDto resultDto);

    /**
     * 소켓 연결 정보를 등록합니다.
     *
     * @param requestId 연결된 클라이언트의 requestId
     */
    void registerConnection(String requestId);
}
