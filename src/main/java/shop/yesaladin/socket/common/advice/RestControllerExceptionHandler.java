package shop.yesaladin.socket.common.advice;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.yesaladin.common.dto.ResponseDto;
import shop.yesaladin.common.exception.ClientException;
import shop.yesaladin.common.exception.ServerException;

@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ResponseDto<Void>> handleServerException(ServerException e) {
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder()
                .success(false)
                .status(e.getResponseStatus())
                .errorMessages(List.of(e.getDisplayErrorMessage()))
                .build();
        return ResponseEntity.status(e.getResponseStatus()).body(responseDto);
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ResponseDto<Void>> handleClientException(ClientException e) {
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder()
                .success(false)
                .status(e.getResponseStatus())
                .errorMessages(List.of(e.getDisplayErrorMessage()))
                .build();
        return ResponseEntity.status(e.getResponseStatus()).body(responseDto);
    }

}
