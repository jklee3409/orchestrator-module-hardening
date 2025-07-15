package eureca.capstone.project.orchestrator.common.exception;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.dto.base.ErrorResponseDto;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BlockOAuthUserException;
import eureca.capstone.project.orchestrator.common.exception.custom.EmailVerifyTokenMismatchException;
import eureca.capstone.project.orchestrator.common.exception.custom.RefreshTokenMismatchException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RefreshTokenMismatchException.class)
    public BaseResponseDto<ErrorResponseDto> handleRefreshTokenMismatchException(RefreshTokenMismatchException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.REFRESH_TOKEN_MISMATCH);
    }

    @ExceptionHandler(EmailVerifyTokenMismatchException.class)
    public BaseResponseDto<ErrorResponseDto> handleEmailVerifyMismatchException(EmailVerifyTokenMismatchException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.EMAIL_TOKEN_MISMATCH);
    }

    @ExceptionHandler(BlockOAuthUserException.class)
    public BaseResponseDto<ErrorResponseDto> handleBlockOAuthUserException(BlockOAuthUserException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.BLOCK_OAUTH_USER);
    }
}
