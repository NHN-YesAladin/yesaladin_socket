package shop.yesaladin.socket.coupon.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.yesaladin.socket.coupon.domain.model.CouponSocketConnection;

class MapCouponSocketConnectionRepositoryTest {

    private MapCouponSocketConnectionRepository repository;
    private ConcurrentHashMap<String, CouponSocketConnection> store;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        repository = new MapCouponSocketConnectionRepository();
        store = new ConcurrentHashMap<>();

        Field connectionMapField = MapCouponSocketConnectionRepository.class.getDeclaredField(
                "connectionMap");

        Field modifiers = connectionMapField.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(connectionMapField, connectionMapField.getModifiers() & ~Modifier.FINAL);

        connectionMapField.setAccessible(true);
        connectionMapField.set(null, store);
    }

    @Test
    @DisplayName("쿠폰 소켓 연결 정보를 저장한다.")
    void saveTest() {
        // given
        CouponSocketConnection expected = new CouponSocketConnection(
                "requestId",
                LocalDateTime.now()
        );

        // when
        repository.save(expected);

        // then
        Assertions.assertThat(store).contains(MapEntry.entry("requestId", expected));
    }

    @Test
    @DisplayName("쿠폰 소켓 연결 정보 존재 여부를 확인한다.")
    void existsByRequestIdTest() {
        // given
        CouponSocketConnection expected = new CouponSocketConnection(
                "requestId",
                LocalDateTime.now()
        );
        CouponSocketConnection unexpected = new CouponSocketConnection(
                "requestId1",
                LocalDateTime.now()
        );
        store.put(expected.getRequestId(), expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        boolean actual = repository.existsByRequestId(expected.getRequestId());

        // then
        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("request id로 쿠폰 소켓 연결 정보를 삭제한다.")
    void deleteByRequestIdTest() {
        // given
        CouponSocketConnection expected = new CouponSocketConnection(
                "requestId",
                LocalDateTime.now()
        );
        CouponSocketConnection unexpected = new CouponSocketConnection(
                "requestId1",
                LocalDateTime.now()
        );
        store.put(expected.getRequestId(), expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        repository.deleteByRequestId("requestId");

        // then
        Assertions.assertThat(store).doesNotContainValue(expected);
        Assertions.assertThat(store).containsValue(unexpected);
    }

    @Test
    @DisplayName("발행된지 30분이 지난 연결 정보를 가져온다.")
    void findAllOver30MinFromIssuedTest() {
        // given
        Clock clock = Clock.fixed(Instant.ofEpochSecond(100000000), ZoneId.of("UTC"));
        CouponSocketConnection expected = new CouponSocketConnection(
                "requestId",
                LocalDateTime.now(clock).minusMinutes(31)
        );
        CouponSocketConnection unexpected = new CouponSocketConnection(
                "requestId1",
                LocalDateTime.now(clock).minusMinutes(29)
        );
        store.put("requestId", expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        List<CouponSocketConnection> actual = repository.findAllOver30MinFromConnected(LocalDateTime.now(
                clock));

        // then
        Assertions.assertThat(actual).hasSize(1);
        Assertions.assertThat(actual).contains(expected);
        Assertions.assertThat(actual).doesNotContain(unexpected);
    }
}