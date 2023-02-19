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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.yesaladin.coupon.code.CouponSocketRequestKind;
import shop.yesaladin.coupon.message.CouponResultDto;

class MapCouponResultMessageRepositoryTest {

    private MapCouponResultMessageRepository repository;
    private ConcurrentHashMap<String, CouponResultDto> store;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        repository = new MapCouponResultMessageRepository();
        store = new ConcurrentHashMap<>();

        Field resultMapField = MapCouponResultMessageRepository.class.getDeclaredField(
                "couponResultMap");

        Field modifierField = resultMapField.getClass().getDeclaredField("modifiers");
        modifierField.setAccessible(true);
        modifierField.setInt(resultMapField, resultMapField.getModifiers() & ~Modifier.FINAL);

        resultMapField.setAccessible(true);
        resultMapField.set(repository, store);
    }

    @Test
    @DisplayName("쿠폰 처리 결과 메시지가 저장된다.")
    void saveTest() {
        // given
        CouponResultDto expected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId",
                true,
                null,
                LocalDateTime.now()
        );

        // when
        repository.save(expected);

        // then
        CouponResultDto actual = store.get("requestId");
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("request id로 존재 여부를 반환한다.")
    void existsByRequestIdTest() {
        // given
        CouponResultDto expected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId",
                true,
                null,
                LocalDateTime.now()
        );
        CouponResultDto unexpected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId1",
                true,
                null,
                LocalDateTime.now()
        );
        store.put("requestId", expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        boolean actual = repository.existsByRequestId("requestId");

        // then
        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("request id로 결과 메시지를 찾아온다.")
    void getByRequestIdTest() {
        // given
        CouponResultDto expected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId",
                true,
                null,
                LocalDateTime.now()
        );
        CouponResultDto unexpected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId1",
                true,
                null,
                LocalDateTime.now()
        );
        store.put("requestId", expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        CouponResultDto actual = repository.getByRequestId("requestId");

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("request id로 메시지를 삭제한다.")
    void deleteByRequestIdTest() {
        // given
        CouponResultDto expected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId",
                true,
                null,
                LocalDateTime.now()
        );
        CouponResultDto unexpected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId1",
                true,
                null,
                LocalDateTime.now()
        );
        store.put("requestId", expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        repository.deleteByRequestId("requestId");

        // then
        Assertions.assertThat(store).doesNotContainValue(expected);
        Assertions.assertThat(store).containsValue(unexpected);
    }

    @Test
    @DisplayName("발행된지 30분이 지난 메시지를 가져온다.")
    void findAllOver30MinFromIssuedTest() {
        // given
        Clock clock = Clock.fixed(Instant.ofEpochSecond(100000000), ZoneId.of("UTC"));
        CouponResultDto expected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId",
                true,
                null,
                LocalDateTime.now(clock).minusMinutes(31)
        );
        CouponResultDto unexpected = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "requestId1",
                true,
                null,
                LocalDateTime.now(clock).minusMinutes(29)
        );
        store.put("requestId", expected);
        store.put(unexpected.getRequestId(), unexpected);

        // when
        List<CouponResultDto> actual = repository.findAllOver30MinFromIssued(LocalDateTime.now(clock));

        // then
        Assertions.assertThat(actual).hasSize(1);
        Assertions.assertThat(actual).contains(expected);
        Assertions.assertThat(actual).doesNotContain(unexpected);
    }
}