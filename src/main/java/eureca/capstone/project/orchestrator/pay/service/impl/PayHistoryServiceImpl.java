package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserPayNotFoundException;
import eureca.capstone.project.orchestrator.common.util.ChangeTypeManager;
import eureca.capstone.project.orchestrator.pay.dto.PayHistoryDto;
import eureca.capstone.project.orchestrator.pay.dto.PayHistoryDto.PayHistorySimpleDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PayHistoryDetailResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.ChangeType;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistoryDetail;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistoryDetail;
import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayHistoryDetail;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryDetailRepository;
import eureca.capstone.project.orchestrator.pay.repository.ExchangeHistoryDetailRepository;
import eureca.capstone.project.orchestrator.pay.repository.PayHistoryDetailRepository;
import eureca.capstone.project.orchestrator.pay.repository.PayHistoryRepository;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayHistoryServiceImpl implements PayHistoryService {
    private final ChargeHistoryDetailRepository chargeHistoryDetailRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final PayHistoryDetailRepository payHistoryDetailRepository;
    private final ChangeTypeManager changeTypeManager;
    private final UserPayRepository userPayRepository;
    private final UserRepository userRepository;
    private final ExchangeHistoryDetailRepository exchangeHistoryDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PayHistorySimpleDto> getPayHistoryList(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        log.info("[getPayHistoryList] 페이 변동 내역 조회 시작. 사용자 ID: {}", user.getUserId());

        Page<PayHistory> historyPage = payHistoryRepository.findPayHistories(user, pageable);
        log.info("[getPayHistoryList] 페이 변동 내역 조회 완료. 총 {}건", historyPage.getTotalElements());

        Page<PayHistoryDto.PayHistorySimpleDto> dtoPage = historyPage.map(PayHistoryDto.PayHistorySimpleDto::fromEntity);
        log.info("[getPayHistoryList] 페이 변동 내역 DTO 변환 완료.");

        return dtoPage;
    }

    @Override
    @Transactional(readOnly = true)
    public PayHistoryDetailResponseDto getPayHistoryDetail(String email, Long payHistoryId) {
        User user = findUserByEmail(email);
        log.info("[getPayHistoryDetail] 페이 변동 내역 상세 조회 시작. 사용자 ID: {}, PayHistory ID: {}", user.getUserId(), payHistoryId);

        PayHistory payHistory = payHistoryRepository.findById(payHistoryId)
                .orElseThrow(() -> {
                    log.error("[getPayHistoryDetail] 존재하지 않는 PayHistory ID: {}", payHistoryId);
                    return new InternalServerException(ErrorCode.PAY_HISTORY_NOT_FOUND);
                });
        log.info("[getPayHistoryDetail] PayHistory ID: {}, 변동 유형: {}", payHistoryId, payHistory.getChangeType().getType());

        var responseBuilder = PayHistoryDetailResponseDto.builder()
                .payHistoryId(payHistory.getPayHistoryId())
                .changeType(payHistory.getChangeType().getType())
                .finalUserPay(payHistory.getFinalPay())
                .createdAt(payHistory.getCreatedAt());

        String type = payHistory.getChangeType().getType();

        switch (type) {
            case "충전":
                ChargeHistory chargeHistory = payHistoryRepository.findChargeHistoryByPayHistoryId(payHistoryId)
                        .orElseThrow(() -> new InternalServerException(ErrorCode.CHARGE_HISTORY_NOT_FOUND));
                responseBuilder.chargeDetail(PayHistoryDetailResponseDto.ChargeDetailDto.fromEntity(chargeHistory));
                log.info("[getPayHistoryDetail] ChargeHistory ID: {}", chargeHistory.getChargeHistoryId());
                break;

            case "환전":
                ExchangeHistory exchangeHistory = payHistoryRepository.findExchangeHistoryByPayHistoryId(payHistoryId)
                        .orElseThrow(() -> new InternalServerException(ErrorCode.EXCHANGE_HISTORY_NOT_FOUND));
                responseBuilder.exchangeDetail(PayHistoryDetailResponseDto.ExchangeDetailDto.fromEntity(exchangeHistory, payHistory.getChangedPay()));
                log.info("[getPayHistoryDetail] ExchangeHistory ID: {}", exchangeHistory.getExchangeHistoryId());
                break;

            case "구매":
            case "판매":
                DataTransactionHistory txHistory = payHistoryRepository.findTransactionHistoryByPayHistoryId(payHistoryId)
                        .orElseThrow(() -> new InternalServerException(ErrorCode.TRANSACTION_HISTORY_NOT_FOUND));
                responseBuilder.transactionDetail(PayHistoryDetailResponseDto.TransactionDetailDto.fromEntity(txHistory, payHistory.getChangedPay()));
                log.info("[getPayHistoryDetail] DataTransactionHistory ID: {}", txHistory.getTransactionHistoryId());
                break;

            default:
                log.error("[getPayHistoryDetail] 지원하지 않는 변동 유형: {}", type);
                throw new InternalServerException(ErrorCode.CHANGE_TYPE_NOT_FOUND);
        }

        return responseBuilder.build();
    }

    @Override
    @Transactional
    public void createChargePayHistory(User user, Long changedPay, Long finalPay, ChargeHistory chargeHistory) {
        log.info("[createChargePayHistory] 페이 충전 변동 내역 기록 시작. 사용자 ID: {}, 충전 내역 ID: {}", user.getUserId(), chargeHistory.getChargeHistoryId());

        ChangeType chargeType = changeTypeManager.getChangeType("충전");
        PayHistory newPayHistory = PayHistory.builder()
                .user(user)
                .changeType(chargeType)
                .changedPay(changedPay)
                .finalPay(finalPay)
                .build();
        payHistoryRepository.save(newPayHistory);
        log.info("[createChargePayHistory] 페이 변동 내역 저장 완료. PayHistory ID: {}", newPayHistory.getPayHistoryId());

        ChargeHistoryDetail detail = ChargeHistoryDetail.builder()
                .payHistory(newPayHistory)
                .chargeHistory(chargeHistory)
                .build();
        chargeHistoryDetailRepository.save(detail);
        log.info("[createChargePayHistory] 충전 상세 내역 저장 완료. ChargeHistoryDetail ID: {}", detail.getChargeHistoryDetailId());
    }

    @Override
    @Transactional
    public void createPurchasePayHistory(User buyer, Long changedPay, DataTransactionHistory txHistory) {
        createPayHistory(buyer, "구매", changedPay, txHistory);
    }

    @Override
    @Transactional
    public void createSalePayHistory(User seller, Long changedPay, DataTransactionHistory txHistory) {
        createPayHistory(seller, "판매", changedPay, txHistory);
    }

    @Override
    public void createExchangePayHistory(User user, Long changedPay, ExchangeHistory exchangeHistory) {
        log.info("[createExchangePayHistory] 페이 환전 변동 내역 기록 시작. 사용자 ID: {}", user.getUserId());

        UserPay userPay = userPayRepository.findById(user.getUserId())
                .orElseThrow(UserPayNotFoundException::new);

        ChangeType exchangeType = changeTypeManager.getChangeType("환전");

        PayHistory newPayHistory = PayHistory.builder()
                .user(user)
                .changeType(exchangeType)
                .changedPay(changedPay)
                .finalPay(userPay.getPay())
                .build();
        payHistoryRepository.save(newPayHistory);
        log.info("[createExchangePayHistory] 페이 변동 내역 저장 완료. PayHistory ID: {}", newPayHistory.getPayHistoryId());

        ExchangeHistoryDetail detail = ExchangeHistoryDetail.builder()
                .payHistory(newPayHistory)
                .exchangeHistory(exchangeHistory)
                .build();
        exchangeHistoryDetailRepository.save(detail);
        log.info("[createExchangePayHistory] 환전 상세 내역 저장 완료. ExchangeHistoryDetail ID: {}", detail.getExchangeHistoryDetailId());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private void createPayHistory(User user, String type, Long changedPay, DataTransactionHistory txHistory) {
        log.info("[createPayHistory] 페이 변동 내역 생성 시작. 사용자 ID: {}, 유형: {}, 변경된 페이: {}", user.getUserId(), type, changedPay);

        UserPay userPay = userPayRepository.findById(user.getUserId()).orElseThrow();
        ChangeType changeType = changeTypeManager.getChangeType(type);
        log.info("[createPayHistory] ChangeType 조회 완료. 유형: {}", changeType.getType());

        PayHistory payHistory = PayHistory.builder()
                .user(user)
                .changeType(changeType)
                .changedPay(changedPay)
                .finalPay(userPay.getPay())
                .build();
        payHistoryRepository.save(payHistory);
        log.info("[createPayHistory] 페이 변동 내역 저장 완료. PayHistory ID: {}", payHistory.getPayHistoryId());

        PayHistoryDetail detail = PayHistoryDetail.builder()
                .payHistory(payHistory)
                .dataTransactionHistory(txHistory)
                .build();
        payHistoryDetailRepository.save(detail);
        log.info("[createPayHistory] 페이 변동 상세 내역 저장 완료. PayHistoryDetail ID: {}", detail.getPayHistoryDetailId());
    }
}
