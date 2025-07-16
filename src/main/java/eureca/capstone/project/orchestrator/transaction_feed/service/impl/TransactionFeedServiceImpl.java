package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionFeedServiceImpl implements TransactionFeedService {
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final UserDataRepositoryCustom userDataRepositoryCustom;
    private final TelecomCompanyRepository telecomCompanyRepository;
    private final SalesTypeRepository salesTypeRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final TransactionFeedRepositoryCustom transactionFeedRepositoryCustom;
    private final UserDataService userDataService;
    private final StatusManager statusManager;

    @Override
    @Transactional
    public CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto) {
        log.info("[createFeed] 사용자 {} 판매글 작성 시작", email);
        try {
            User user = findUserByEmail(email);
            UserData userData = findAndValidateUserData(user, feedRequestDto.getSalesDataAmount());
            log.info("[createFeed] 사용자 판매 가능 데이터 검증 완료.");

            TransactionFeed transactionFeed = buildNewFeed(user, userData, feedRequestDto);
            transactionFeedRepository.save(transactionFeed);
            log.info("[createFeed] 판매글 DB 저장 완료. 판매글 ID: {}", transactionFeed.getTransactionFeedId());

            userDataService.deductSellableData(user.getUserId(), feedRequestDto.getSalesDataAmount());
            log.info("[createFeed] 판매자 판매 가능 데이터 차감 완료.");

            return CreateFeedResponseDto.builder()
                    .id(transactionFeed.getTransactionFeedId())
                    .build();

        } catch (Exception e) {
            log.info("[createFeed] 판매글 작성 도중 오류 발생");
            throw new InternalServerException(ErrorCode.TRANSACTION_FEED_CREATE_FAIL);
        }
    }

    @Override
    @Transactional
    public UpdateFeedResponseDto updateFeed(String email, UpdateFeedRequestDto updateFeedRequestDto) {
        log.info("[updateFeed] 판매글 수정 시작. 사용자: {}, 판매글 ID: {}", email, updateFeedRequestDto.getTransactionFeedId());
        try {
            User user = findUserByEmail(email);
            TransactionFeed transactionFeed = findTransactionFeedById(updateFeedRequestDto.getTransactionFeedId());
            log.info("[updateFeed] 수정하려는 판매글 ID: {}", transactionFeed.getTransactionFeedId());

            if (!transactionFeed.getUser().equals(user)) throw new FeedModifyPermissionException();

            handleSaleDataChange(user, transactionFeed.getSalesDataAmount(), updateFeedRequestDto.getSalesDataAmount());

            transactionFeed.update(
                    updateFeedRequestDto.getTitle(),
                    updateFeedRequestDto.getContent(),
                    updateFeedRequestDto.getSalesPrice(),
                    updateFeedRequestDto.getSalesDataAmount(),
                    updateFeedRequestDto.getDefaultImageNumber()
            );

            log.info("[updateFeed] 판매글 DB 업데이트 완료. 판매글 ID: {}", transactionFeed.getTransactionFeedId());

            return UpdateFeedResponseDto.builder()
                    .transactionFeedId(transactionFeed.getTransactionFeedId())
                    .build();

        } catch (Exception e) {
            log.info("[updateFeed] 판매글 수정 도중 오류 발생");
            throw new InternalServerException(ErrorCode.TRANSACTION_FEED_UPDATE_FAIL);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepositoryCustom.findByIdWithLock(transactionFeedId) // 동시성 제어를 위해 비관적 락 사용
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private UserData findAndValidateUserData(User user, long salesDataAmount) {
        UserData userData = userDataRepositoryCustom.findByUserIdWithLock(user.getUserId())
                .orElseThrow(UserDataNotFoundException::new);

        if (userData.getSellableDataMb() < salesDataAmount) {
            throw new DataOverSellableAmountException();
        }
        return userData;
    }

    private TransactionFeed buildNewFeed(User user, UserData userData, CreateFeedRequestDto dto) {
        TelecomCompany telecomCompany = telecomCompanyRepository.findById(dto.getTelecomCompanyId())
                .orElseThrow(TelecomCompanyNotFoundException::new);

        if (user.getTelecomCompany() != telecomCompany) throw new InvalidTelecomCompanyException();

        SalesType salesType = salesTypeRepository.findById(dto.getSalesTypeId())
                .orElseThrow(StatusNotFoundException::new);

        Status status = statusManager.getStatus("FEED", "ON_SALE");

        LocalDateTime expiresAt = calculateExpirationDate(userData.getResetDataAt());

        return TransactionFeed.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .telecomCompany(telecomCompany)
                .salesType(salesType)
                .salesPrice(dto.getSalesPrice())
                .salesDataAmount(dto.getSalesDataAmount())
                .defaultImageNumber(dto.getDefaultImageNumber())
                .expiresAt(expiresAt)
                .status(status)
                .isDeleted(false)
                .build();
    }

    private LocalDateTime calculateExpirationDate(int resetDay) {
        LocalDate today = LocalDate.now();
        LocalDate nextReset = today.withDayOfMonth(resetDay);

        if (today.getDayOfMonth() >= resetDay) {
            nextReset = nextReset.plusMonths(1);
        }
        return nextReset.minusDays(1).atTime(23, 59, 59);
    }

    /**
     * 판매글 수정시 판매 데이터 양 변경에 따른 사용자 데이터 가감산을 처리합니다.
     *
     * @param user 사용자 엔티티
     * @param originalSaleDataMb 기존 판매 데이터 양
     * @param newSaleDataMb 새로운 판매 데이터 양
     */
    private void handleSaleDataChange(User user, long originalSaleDataMb, long newSaleDataMb) {
        long saleDataChangeAmount = newSaleDataMb - originalSaleDataMb;

        if (saleDataChangeAmount > 0) {
            log.info("[handleSaleDataChange] 판매 데이터 증가. 추가량: {}", saleDataChangeAmount);
            findAndValidateUserData(user, saleDataChangeAmount);
            userDataService.deductSellableData(user.getUserId(), saleDataChangeAmount);
            log.info("[handleSaleDataChange] 사용자 판매 가능 데이터 추가 차감 완료.");

        } else if (saleDataChangeAmount < 0) {
            long refundDataAmount = Math.abs(saleDataChangeAmount);
            log.info("[handleSaleDataChange] 판매 데이터 감소. 환불량: {}", refundDataAmount);
            userDataService.addSellableData(user.getUserId(), refundDataAmount);
            log.info("[handleSaleDataChange] 사용자 판매 가능 데이터 환불 완료");

        } else {
            log.info("[handleSaleDataChange] 판매 데이터 양 변경 없음.");
        }
    }
}
