package eureca.capstone.project.orchestrator.common.exception;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.dto.base.ErrorResponseDto;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.exception.custom.BlackListUserException;
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

    @ExceptionHandler(BlackListUserException.class)
    public BaseResponseDto<ErrorResponseDto> handleBlockOAuthUserException(BlackListUserException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.BLACK_LIST_USER_FOUND);
    }

    @ExceptionHandler(FeedModifyPermissionException.class)
    public BaseResponseDto<ErrorResponseDto> handleFeedModifyPermissionException(FeedModifyPermissionException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.FEED_MODIFY_PERMISSION_DENIED);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public BaseResponseDto<ErrorResponseDto> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @ExceptionHandler(InternalServerException.class)
    public BaseResponseDto<ErrorResponseDto> handleInternalServerException(InternalServerException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(e.getErrorCode());
    }

    @ExceptionHandler(DataOverSellableAmountException.class)
    public BaseResponseDto<ErrorResponseDto> handleDataOverSellableAmountException(DataOverSellableAmountException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.USER_SELLABLE_DATA_LACK);
    }

    @ExceptionHandler(EmptyPlanException.class)
    public BaseResponseDto<ErrorResponseDto> handleEmptyPlanException(EmptyPlanException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.EMPTY_PLAN);
    }

    @ExceptionHandler(InvalidTelecomCompanyException.class)
    public BaseResponseDto<ErrorResponseDto> handleInvalidTelecomCompanyException(InvalidTelecomCompanyException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.INVALID_TELECOM_COMPANY);
    }

    @ExceptionHandler(TelecomCompanyNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleTelecomCompanyNotFoundException(TelecomCompanyNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.TELECOM_COMPANY_NOT_FOUND);
    }

    @ExceptionHandler(StatusNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleStatusNotFoundException(StatusNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.STATUS_NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(TransactionFeedNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleTransactionFeedNotFoundException(TransactionFeedNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.TRANSACTION_FEED_NOT_FOUND);
    }

    @ExceptionHandler(UserDataNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleUserDataNotFoundException(UserDataNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.USER_DATA_NOT_FOUND);
    }

    @ExceptionHandler(AuctionCreationNotAllowedException.class)
    public BaseResponseDto<ErrorResponseDto> handleAuctionCreationNotAllowedException(AuctionCreationNotAllowedException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.AUCTION_FEED_CREATE_FAIL);
    }

    @ExceptionHandler(SalesTypeNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleSalesTypeNotFoundException(SalesTypeNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.SALES_TYPE_NOT_FOUND);
    }
}