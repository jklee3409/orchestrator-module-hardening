package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UseDataCouponResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataCouponRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.UserDataCouponRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataCouponService;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.AddBuyerDataResponseDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCouponServiceImpl implements DataCouponService {
    private final DataCouponRepository dataCouponRepository;
    private final UserDataCouponRepository userDataCouponRepository;
    private final UserRepository userRepository;
    private final UserDataService userDataService;
    private final StatusManager statusManager;

    @Override
    @Transactional
    public void issueDataCoupon(User buyer, TransactionFeed purchaseFeed) {
        log.info("[issueDataCoupon] 사용자: {}, 판매글: {} 에 대한 데이터 쿠폰 발급 시작", buyer.getUserId(), purchaseFeed.getTransactionFeedId());

        Long dataAmount = purchaseFeed.getSalesDataAmount();
        TelecomCompany telecomCompany = purchaseFeed.getTelecomCompany();
        log.info("[issueDataCoupon] 데이터 양: {}, 통신사: {}", dataAmount, telecomCompany);

        DataCoupon dataCoupon = findOrCreateDataCoupon(dataAmount, telecomCompany);
        log.info("[issueDataCoupon] 데이터 쿠폰 준비 완료. 쿠폰 ID: {}", dataCoupon.getDataCouponId());

        Status issuedStatus = statusManager.getStatus("COUPON", "ISSUED");

        UserDataCoupon userDataCoupon = UserDataCoupon.builder()
                .user(buyer)
                .dataCoupon(dataCoupon)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .status(issuedStatus)
                .build();
        log.info("[issueDataCoupon] 사용자 데이터 쿠폰 생성: 사용자 ID: {}, 쿠폰 ID: {}", buyer.getUserId(), userDataCoupon.getUserDataCouponId());

        userDataCouponRepository.save(userDataCoupon);
        log.info("[issueDataCoupon] 사용자 데이터 쿠폰 저장 완료: 사용자 ID: {}, 쿠폰 ID: {}", buyer.getUserId(), userDataCoupon.getUserDataCouponId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDataCouponDto> getUserDataCouponList(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        log.info("[getUserDataCouponList] 사용자: {} 에 대한 데이터 쿠폰 목록 조회 시작", user.getUserId());

        Page<UserDataCoupon> userDataCouponsPage = userDataCouponRepository.findDetailsByUser(user, pageable);
        log.info("[getUserDataCouponList] 사용자: {} 에 대한 데이터 쿠폰 목록 조회 완료. 총 {} 페이지 중 {} 페이지, 총 쿠폰 개수: {}",
                user.getUserId(), userDataCouponsPage.getTotalPages(), pageable.getPageNumber(),
                userDataCouponsPage.getTotalElements());

        return userDataCouponsPage.map(UserDataCouponDto::fromEntity);
    }

    @Override
    @Transactional
    public UseDataCouponResponseDto useDataCoupon(String email, Long userDataCouponId) {
        log.info("[useDataCoupon] 사용자 {}의 쿠폰 {} 사용 요청", email, userDataCouponId);
        User user = findUserByEmail(email);

        UserDataCoupon userDataCoupon = userDataCouponRepository.findDetailsById(userDataCouponId)
                .orElseThrow(() -> new InternalServerException(ErrorCode.DATA_COUPON_NOT_FOUND));

        // 1. 쿠폰 소유권 검증
        if (!Objects.equals(userDataCoupon.getUser().getUserId(), user.getUserId())) {
            log.warn("[useDataCoupon] 쿠폰 소유권 없음. 쿠폰 소유자 ID: {}, 요청자 ID: {}", userDataCoupon.getUser().getUserId(), user.getUserId());
            throw new InternalServerException(ErrorCode.DATA_COUPON_ACCESS_DENIED);
        }

        // 2. 쿠폰 상태 검증
        Status issuedStatus = statusManager.getStatus("COUPON", "ISSUED");
        if (!userDataCoupon.getStatus().getCode().equals(issuedStatus.getCode())) {
            log.warn("[useDataCoupon] 이미 사용되었거나 유효하지 않은 상태의 쿠폰. 현재 상태: {}", userDataCoupon.getStatus().getCode());
            throw new InternalServerException(ErrorCode.DATA_COUPON_ALREADY_USED);
        }

        // 3. 유효 기간 검증
        if (userDataCoupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("[useDataCoupon] 만료된 쿠폰. 만료일: {}", userDataCoupon.getExpiresAt());
            throw new InternalServerException(ErrorCode.DATA_COUPON_EXPIRED);
        }

        // 4. 사용자 구매 데이터 충전
        Long dataAmount = userDataCoupon.getDataCoupon().getDataAmount();
        log.info("[useDataCoupon] 사용자 {}에게 데이터 {}MB 충전 시작", user.getUserId(), dataAmount);
        AddBuyerDataResponseDto chargeResponse = userDataService.chargeBuyerData(user.getUserId(), dataAmount);
        log.info("[useDataCoupon] 데이터 충전 완료. 최종 구매 데이터: {}", chargeResponse.getBuyerDataMb());

        // 5. 쿠폰 상태 'USED'로 변경
        Status usedStatus = statusManager.getStatus("COUPON", "USED");
        userDataCoupon.updateStatus(usedStatus);
        log.info("[useDataCoupon] 쿠폰 {}의 상태를 'USED'로 변경 완료", userDataCouponId);

        return UseDataCouponResponseDto.builder()
                .userDataCouponId(userDataCouponId)
                .buyerDataMb(chargeResponse.getBuyerDataMb())
                .build();
    }

    private DataCoupon findOrCreateDataCoupon(Long dataAmount, TelecomCompany telecomCompany) {
        return dataCouponRepository.findByDataAmountAndTelecomCompany(dataAmount, telecomCompany)
                .orElseGet(() -> {
                    log.info("[findOrCreateDataCoupon] {}MB ({})에 대한 새 데이터 쿠폰 생성", dataAmount, telecomCompany.getName());
                    DataCoupon newDataCoupon = DataCoupon.builder()
                            .dataAmount(dataAmount)
                            .telecomCompany(telecomCompany)
                            .couponNumber(generateCouponNumber())
                            .build();
                    return dataCouponRepository.save(newDataCoupon);
                });
    }

    private String generateCouponNumber() {
        return UUID.randomUUID().toString();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
