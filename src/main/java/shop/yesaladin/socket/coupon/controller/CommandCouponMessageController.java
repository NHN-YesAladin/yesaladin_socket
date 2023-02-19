package shop.yesaladin.socket.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.yesaladin.common.dto.ResponseDto;
import shop.yesaladin.coupon.message.CouponResultDto;
import shop.yesaladin.socket.coupon.service.inter.CouponWebsocketMessageService;

/**
 * 소켓 서버에 쿠폰 관련 메시지를 등록하기 위한 컨트롤러 클래스입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/coupon-messages")
public class CommandCouponMessageController {

    private final CouponWebsocketMessageService couponWebsocketMessageService;

    /**
     * 쿠폰 지급 / 사용 결과 메시지를 소켓 서버에 등록하고 전송을 시도합니다.
     *
     * @param message 전송을 시도할 메시지
     * @return 전송 / 등록 결과
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<Void> registerMessage(@RequestBody CouponResultDto message) {
        couponWebsocketMessageService.trySendGiveCouponResultMessage(message);

        return ResponseDto.<Void>builder().status(HttpStatus.CREATED).success(true).build();
    }

}
