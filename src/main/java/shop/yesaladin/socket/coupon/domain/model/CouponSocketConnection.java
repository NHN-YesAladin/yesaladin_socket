package shop.yesaladin.socket.coupon.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponSocketConnection {

    private final String requestId;

    private final LocalDateTime connectedDateTime;
}
