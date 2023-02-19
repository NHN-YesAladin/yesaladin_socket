package shop.yesaladin.socket.coupon.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import shop.yesaladin.coupon.code.CouponSocketRequestKind;
import shop.yesaladin.coupon.message.CouponResultDto;
import shop.yesaladin.socket.config.SocketProperties;
import shop.yesaladin.socket.coupon.domain.repository.CouponResultMessageRepository;
import shop.yesaladin.socket.coupon.domain.repository.CouponSocketConnectionRepository;

class CouponWebsocketMessageServiceImplTest {

    private CouponResultMessageRepository couponResultMessageRepository;
    private CouponSocketConnectionRepository couponSocketConnectionRepository;
    private SimpMessageSendingOperations messagingTemplate;
    private SocketProperties socketProperties;
    private Clock clock;
    private CouponWebsocketMessageServiceImpl service;

    @BeforeEach
    void setUp() {
        couponResultMessageRepository = Mockito.mock(CouponResultMessageRepository.class);
        couponSocketConnectionRepository = Mockito.mock(CouponSocketConnectionRepository.class);
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        socketProperties = Mockito.mock(SocketProperties.class);
        clock = Clock.fixed(Instant.ofEpochSecond(100000), ZoneId.of("UTC"));
        service = new CouponWebsocketMessageServiceImpl(
                couponResultMessageRepository,
                couponSocketConnectionRepository,
                messagingTemplate,
                socketProperties,
                clock
        );
    }

    @Test
    @DisplayName("연결된 소켓 정보가 존재하면 메시지를 즉시 발행한다.")
    void trySendGiveCouponResultMessageSendTest() {
        // given
        CouponResultDto message = new CouponResultDto(
                CouponSocketRequestKind.GIVE,
                "requestId",
                true,
                null,
                LocalDateTime.now()
        );
        Mockito.when(couponSocketConnectionRepository.existsByRequestId("requestId"))
                .thenReturn(true);
        Mockito.when(socketProperties.getCouponGiveResultTopicPrefix()).thenReturn("expectedTopic");

        // when
        service.trySendGiveCouponResultMessage(message);

        // then
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .existsByRequestId("requestId");
        Mockito.verify(messagingTemplate, Mockito.times(1))
                .convertAndSend("expectedTopicrequestId", message);
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .deleteByRequestId("requestId");
        Mockito.verify(couponResultMessageRepository, Mockito.times(1))
                .deleteByRequestId("requestId");
        Mockito.verify(couponResultMessageRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @DisplayName("연결된 소켓 정보가 존재하지 않으면 메시지를 발행하지 않고 저장한다.")
    void trySendGiveCouponResultMessageSaveTest() {
        // given
        CouponResultDto message = new CouponResultDto(
                CouponSocketRequestKind.GIVE,
                "requestId",
                true,
                null,
                LocalDateTime.now()
        );
        Mockito.when(couponSocketConnectionRepository.existsByRequestId("requestId"))
                .thenReturn(false);

        // when
        service.trySendGiveCouponResultMessage(message);

        // then
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .existsByRequestId("requestId");
        Mockito.verify(messagingTemplate, Mockito.never())
                .convertAndSend(Mockito.anyString(), Mockito.any(CouponResultDto.class));
        Mockito.verify(couponResultMessageRepository, Mockito.times(1)).save(message);
    }

    @Test
    @DisplayName("소켓이 연결되면 연결 정보를 저장하고 메시지가 존재한다면 전송한다.")
    void registerSocketConnectionSendTest() {
        // given
        String requestId = "requestId";
        CouponResultDto message = new CouponResultDto(
                CouponSocketRequestKind.USE,
                requestId,
                true,
                null,
                LocalDateTime.now()
        );
        Mockito.when(couponSocketConnectionRepository.existsByRequestId(requestId))
                .thenReturn(true);
        Mockito.when(couponResultMessageRepository.getByRequestId(requestId)).thenReturn(message);
        Mockito.when(couponResultMessageRepository.existsByRequestId(requestId)).thenReturn(true);
        Mockito.when(socketProperties.getCouponUseResultTopicPrefix()).thenReturn("expectedTopic");

        // when
        service.registerConnection(requestId);

        // then
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .save(Mockito.argThat(arg -> arg.getRequestId().equals(requestId)
                        && arg.getConnectedDateTime().equals(LocalDateTime.now(clock))));
        Mockito.verify(couponResultMessageRepository, Mockito.times(1))
                .existsByRequestId(requestId);
        Mockito.verify(couponResultMessageRepository, Mockito.times(1)).getByRequestId(requestId);
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .existsByRequestId("requestId");
        Mockito.verify(messagingTemplate, Mockito.times(1))
                .convertAndSend("expectedTopicrequestId", message);
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .deleteByRequestId("requestId");
        Mockito.verify(couponResultMessageRepository, Mockito.times(1))
                .deleteByRequestId("requestId");
    }

    @Test
    @DisplayName("소켓이 연결되면 연결 정보를 저장하고 메시지가 존재하지 않는다면 아무 것도 하지 않는다.")
    void registerSocketConnectionWithoutSendTest() {
        // given
        String requestId = "requestId";
        Mockito.when(couponResultMessageRepository.existsByRequestId(requestId)).thenReturn(false);

        // when
        service.registerConnection(requestId);

        // then
        Mockito.verify(couponSocketConnectionRepository, Mockito.times(1))
                .save(Mockito.argThat(arg -> arg.getRequestId().equals(requestId)
                        && arg.getConnectedDateTime().equals(LocalDateTime.now(clock))));
        Mockito.verify(couponResultMessageRepository, Mockito.times(1))
                .existsByRequestId(requestId);
        Mockito.verify(couponResultMessageRepository, Mockito.never()).getByRequestId(requestId);
        Mockito.verify(couponSocketConnectionRepository, Mockito.never())
                .existsByRequestId("requestId");
        Mockito.verify(messagingTemplate, Mockito.never())
                .convertAndSend(Mockito.anyString(), Mockito.any(CouponResultDto.class));
        Mockito.verify(couponSocketConnectionRepository, Mockito.never())
                .deleteByRequestId("requestId");
        Mockito.verify(couponResultMessageRepository, Mockito.never())
                .deleteByRequestId("requestId");
    }
}