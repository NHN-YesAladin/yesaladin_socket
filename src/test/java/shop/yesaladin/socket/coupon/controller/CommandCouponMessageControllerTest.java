package shop.yesaladin.socket.coupon.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static shop.yesaladin.socket.docs.ApiDocumentUtils.getDocumentRequest;
import static shop.yesaladin.socket.docs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.yesaladin.coupon.code.CouponSocketRequestKind;
import shop.yesaladin.coupon.message.CouponResultDto;
import shop.yesaladin.socket.coupon.service.inter.CouponWebsocketMessageService;

@WebMvcTest(CommandCouponMessageController.class)
@AutoConfigureRestDocs
class CommandCouponMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CouponWebsocketMessageService couponWebsocketMessageService;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("쿠폰 지급/사용 결과 메시지를 소켓 서버에 등록하고 전송을 시도한다.")
    void registerMessageTest() throws Exception {
        // given
        CouponResultDto couponResultDto = new CouponResultDto(
                CouponSocketRequestKind.USE,
                "337d8520-198f-4cc2-a1e3-7559c1c532b0",
                true,
                null,
                LocalDateTime.of(2023, 2, 19, 12, 30)
        );
        String requestBody = objectMapper.writeValueAsString(couponResultDto);

        // when
        ResultActions actual = mockMvc.perform(post("/v1/coupon-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        actual.andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.errorMessages").isEmpty());
        Mockito.verify(couponWebsocketMessageService, Mockito.times(1))
                .trySendGiveCouponResultMessage(Mockito.any());

        // docs
        actual.andDo(document(
                "register-coupon-message",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("requestKind").type(JsonFieldType.STRING)
                                .description("요청 종류(사용 / 지급)"),
                        fieldWithPath("requestId").type(JsonFieldType.STRING).description("요청 ID"),
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                        fieldWithPath("message").type(JsonFieldType.VARIES)
                                .description("요청 관련 메시지. 성공시 null,"),
                        fieldWithPath("issuedDateTime").type(JsonFieldType.STRING)
                                .description("메시지 발행 시간 시간")
                )
        ));

    }
}