package eureca.capstone.project.orchestrator.exception;

import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.response.ErrorResponseDto;
import eureca.capstone.project.orchestrator.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.exception.custom.NotSuccessStatusCodeException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotSuccessStatusCodeException.class)
    public BaseResponseDto<ErrorResponseDto> handleRefreshTokenMismatchException(NotSuccessStatusCodeException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.NOT_SUCCESS_STATUS_CODE_EXCEPTION);
    }
}
