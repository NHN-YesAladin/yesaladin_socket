package shop.yesaladin.socket.coupon.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import shop.yesaladin.socket.coupon.domain.model.CouponSocketConnection;
import shop.yesaladin.socket.coupon.domain.repository.CouponSocketConnectionRepository;

/**
 * CouponGiveSocketConnectionRepository 인터페이스의 ConcurrentHashMap을 사용한 구현체입니다.
 *
 * @author 김홍대
 * @since 1.0
 */
@Repository
public class MapCouponSocketConnectionRepository implements CouponSocketConnectionRepository {

    private static final Map<String, CouponSocketConnection> connectionMap = new ConcurrentHashMap<>();

    @Override
    public void save(CouponSocketConnection connection) {
        connectionMap.put(connection.getRequestId(), connection);
    }

    @Override
    public boolean existsByRequestId(String requestId) {
        return Objects.nonNull(connectionMap.get(requestId));
    }

    @Override
    public void deleteByRequestId(String requestId) {
        connectionMap.remove(requestId);
    }

    @Override
    public List<CouponSocketConnection> findAllOver30MinFromConnected(LocalDateTime now) {
        return connectionMap.values()
                .stream()
                .filter(connection -> now.isAfter(connection.getConnectedDateTime()
                        .plusMinutes(30)))
                .collect(Collectors.toList());
    }

}
