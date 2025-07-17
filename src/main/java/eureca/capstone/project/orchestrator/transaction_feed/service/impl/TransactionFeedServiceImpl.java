package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.SalesTypeDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
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
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

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
    private final SalesTypeManager salesTypeManager;

    @Override
    @Transactional
    public CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto) {
        log.info("[createFeed] 사용자 {} 판매글 작성 시작", email);
        SalesType salesType = salesTypeRepository.findById(feedRequestDto.getSalesTypeId())
                .orElseThrow(StatusNotFoundException::new);

        validateAuctionCreationTime(salesType);
        log.info("[createFeed] 판매글 등록 시간 검증 완료.");

        User user = findUserByEmail(email);
        UserData userData = findAndValidateUserData(user, feedRequestDto.getSalesDataAmount());
        log.info("[createFeed] 사용자 판매 가능 데이터 검증 완료.");

        TransactionFeed transactionFeed = buildNewFeed(user, userData, feedRequestDto, salesType);
        transactionFeedRepository.save(transactionFeed);
        log.info("[createFeed] 판매글 DB 저장 완료. 판매글 ID: {}", transactionFeed.getTransactionFeedId());

        userDataService.deductSellableData(user.getUserId(), feedRequestDto.getSalesDataAmount());
        log.info("[createFeed] 판매자 판매 가능 데이터 차감 완료.");

        return CreateFeedResponseDto.builder()
                .id(transactionFeed.getTransactionFeedId())
                .build();

    }

    @Override
    @Transactional
    public UpdateFeedResponseDto updateFeed(String email, UpdateFeedRequestDto updateFeedRequestDto) {
        log.info("[updateFeed] 판매글 수정 시작. 사용자: {}, 판매글 ID: {}", email, updateFeedRequestDto.getTransactionFeedId());

        User user = findUserByEmail(email);
        TransactionFeed transactionFeed = findTransactionFeedById(updateFeedRequestDto.getTransactionFeedId());
        log.info("[updateFeed] 수정하려는 판매글 ID: {}", transactionFeed.getTransactionFeedId());

        if (!transactionFeed.getUser().equals(user)) throw new FeedModifyPermissionException();
        log.info("[updateFeed] 사용자와 판매자가 일치합니다.");

        if (transactionFeed.getSalesType().getName().equals(salesTypeManager.getBidSaleType().getName())) throw new AuctionTypeModifyNotAllowedException();
        log.info("[updateFeed] 입찰 판매가 아닙니다.");

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
    }

    @Override
    @Transactional(readOnly = true)
    public GetFeedDetailResponseDto getFeedDetail(Long transactionFeedId) {
        log.info("[getFeedDetail] 판매글 상세 조회 시작. ID: {}", transactionFeedId);
        TransactionFeed feed = transactionFeedRepositoryCustom.findFeedDetailById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);

        // TODO: 찜 횟수 별도 조회 필요
        long likedCount = 20L;
        Long currentHeightPrice = null;

        String auctionType = salesTypeManager.getBidSaleType().getName();
        if (auctionType.equals(feed.getSalesType().getName())) {
            log.info("[getFeedDetail] 입찰 판매글입니다. 현재 최고가 조회");
            // TODO: 현재 최고가 조회 필요
            currentHeightPrice = 10000L;
        }

        return GetFeedDetailResponseDto.builder()
                .transactionFeedId(feed.getTransactionFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .salesDataAmount(feed.getSalesDataAmount())
                .salesPrice(feed.getSalesPrice())
                .defaultImageNumber(feed.getDefaultImageNumber())
                .createdAt(feed.getCreatedAt())
                .nickname(feed.getUser().getNickname())
                .likedCount(likedCount)
                .telecomCompany(TelecomCompanyDto.fromEntity(feed.getTelecomCompany()))
                .status(StatusDto.fromEntity(feed.getStatus()))
                .salesType(SalesTypeDto.fromEntity(feed.getSalesType()))
                .expiredAt(feed.getExpiresAt())
                .currentHeightPrice(currentHeightPrice)
                .build();
    }

    @Override
    @Transactional
    public void deleteFeed(String email, Long transactionFeedId) {
        log.info("[deleteFeed] 사용자 {}, 판매글 {} 삭제 시작", email, transactionFeedId);

        TransactionFeed transactionFeed = findTransactionFeedById(transactionFeedId);
        log.info("[deleteFeed] 삭제하려는 판매글 조회 완료.");

        if (!transactionFeed.getUser().getEmail().equals(email)) throw new FeedModifyPermissionException();
        log.info("[deleteFeed] 사용자와 판매자가 일치합니다.");

        if (transactionFeed.getSalesType().getName().equals(salesTypeManager.getBidSaleType().getName())) throw new AuctionTypeModifyNotAllowedException();
        log.info("[deleteFeed] 입찰 판매가 아닙니다.");

        if (transactionFeed.isDeleted()) {
            log.warn("[deleteFeed] 이미 삭제된 판매글(ID: {})에 대한 요청입니다.", transactionFeedId);
            return;
        }

        transactionFeed.delete();
        log.info("[deleteFeed] 판매글 삭제 완료");
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepositoryCustom.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private void validateAuctionCreationTime(SalesType salesType) {
        if (salesTypeManager.getBidSaleType().getName().equals(salesType.getName())) {
            LocalTime now = LocalTime.now();
            LocalTime restrictionStartTime = LocalTime.of(23, 30);

            if (!now.isBefore(restrictionStartTime)) {
                log.info("[validateAuctionCreationTime] 입찰 판매글 등록 제한 시간입니다. 현재시간: {}", now);
                throw new AuctionCreationNotAllowedException();
            }
        }
    }

    private UserData findAndValidateUserData(User user, long salesDataAmount) {
        UserData userData = userDataRepositoryCustom.findByUserIdWithLock(user.getUserId())
                .orElseThrow(UserDataNotFoundException::new);

        if (userData.getSellableDataMb() < salesDataAmount) {
            throw new DataOverSellableAmountException();
        }
        return userData;
    }

    private TransactionFeed buildNewFeed(User user, UserData userData, CreateFeedRequestDto dto, SalesType salesType) {
        TelecomCompany telecomCompany = telecomCompanyRepository.findById(dto.getTelecomCompanyId())
                .orElseThrow(TelecomCompanyNotFoundException::new);

        if (user.getTelecomCompany() != telecomCompany) {
            throw new InvalidTelecomCompanyException();
        }

        Status status = statusManager.getStatus("FEED", "ON_SALE");

        LocalDateTime expiresAt;

        if (salesTypeManager.getBidSaleType().getName().equals(salesType.getName())) {
            log.info("[buildNewFeed] 입찰 판매글이므로 만료일은 오늘 자정입니다.");
            expiresAt = LocalDate.now().atTime(LocalTime.MAX);

        } else {
            log.info("[buildNewFeed] 일반 판매글이므로 만료일은 사용자 데이터 리셋일 기준입니다.");
            expiresAt = calculateExpirationDate(userData.getResetDataAt());
        }

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
     * 판매글 수정시 판매 데이터 양 변경에 따른 사용자 데이터 증가, 감소를 처리합니다.
     *
     * @param user               사용자 엔티티
     * @param originalSaleDataMb 기존 판매 데이터 양
     * @param newSaleDataMb      새로운 판매 데이터 양
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
