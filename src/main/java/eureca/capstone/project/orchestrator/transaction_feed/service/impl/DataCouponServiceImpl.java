package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetUserDataCouponListResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataCouponRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.UserDataCouponRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataCouponService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCouponServiceImpl implements DataCouponService {
    private final DataCouponRepository dataCouponRepository;
    private final UserDataCouponRepository userDataCouponRepository;
    private final UserRepository userRepository;
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
    public GetUserDataCouponListResponseDto getUserDataCouponList(String email) {
        User user = findUserByEmail(email);
        log.info("[getUserDataCouponList] 사용자: {} 에 대한 데이터 쿠폰 목록 조회 시작", user.getUserId());

        List<UserDataCoupon> userDataCoupons = userDataCouponRepository.findDetailsByUser(user);
        log.info("[getUserDataCouponList] 사용자: {} 에 대한 데이터 쿠폰 목록 조회 완료. 쿠폰 개수: {}", user.getUserId(), userDataCoupons.size());

        List<UserDataCouponDto> dtoList = userDataCoupons.stream()
                .map(UserDataCouponDto::fromEntity)
                .toList();
        log.info("[getUserDataCouponList] 사용자: {} 에 대한 데이터 쿠폰 DTO 변환 완료", user.getUserId());

        return GetUserDataCouponListResponseDto.builder()
                .dataCoupons(dtoList)
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
