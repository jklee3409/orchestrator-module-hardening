package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.util.ChangeTypeManager;
import eureca.capstone.project.orchestrator.pay.entity.ChangeType;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistoryDetail;
import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryDetailRepository;
import eureca.capstone.project.orchestrator.pay.repository.PayHistoryRepository;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
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
    private final ChangeTypeManager changeTypeManager;

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
}
