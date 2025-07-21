package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.util.ChangeTypeManager;
import eureca.capstone.project.orchestrator.pay.entity.ChangeType;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistoryDetail;
import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayHistoryDetail;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryDetailRepository;
import eureca.capstone.project.orchestrator.pay.repository.PayHistoryDetailRepository;
import eureca.capstone.project.orchestrator.pay.repository.PayHistoryRepository;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
