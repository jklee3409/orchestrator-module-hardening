package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.EmptyPlanException;
import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import eureca.capstone.project.orchestrator.user.repository.PlanRepository;
import eureca.capstone.project.orchestrator.user.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    /**
     * 통신사에 따른 랜덤 요금제를 조회합니다.
     * 지정된 통신사에 해당하는 요금제 중 하나를 무작위로 선택하여 반환합니다.
     * 사용자 등록 시 초기 요금제 할당에 사용됩니다.
     *
     * @param randomPlanRequestDto 통신사 정보
     * @return 선택된 요금제 ID와 월간 데이터 용량
     * @throws EmptyPlanException 해당 통신사에 등록된 요금제가 없는 경우
     */
    @Override
    public RandomPlanResponseDto getRandomPlan(RandomPlanRequestDto randomPlanRequestDto) {
        log.info("[getRandomPlan] 랜덤 요금제 요청");

        Page<Plan> planPage = planRepository.findRandomPlanByTelecomCompany(
                randomPlanRequestDto.getTelecomCompany(),
                PageRequest.of(0, 1)
        );

        Plan randomPlan = planPage.get().findFirst()
                .orElseThrow(EmptyPlanException::new);

        log.info("[getRandomPlan] 랜덤 요금제 선택: planId={}, monthlyDataMb={}", randomPlan.getPlanId(), randomPlan.getMonthlyDataMb());

        return RandomPlanResponseDto.builder()
                .planId(randomPlan.getPlanId())
                .monthlyDataMb(randomPlan.getMonthlyDataMb())
                .build();
    }
}
