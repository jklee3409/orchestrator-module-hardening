package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.EmptyPlanException;
import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import eureca.capstone.project.orchestrator.user.repository.PlanRepository;
import eureca.capstone.project.orchestrator.user.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    public RandomPlanResponseDto getRandomPlan(RandomPlanRequestDto randomPlanRequestDto) {
        log.info("[getRandomPlan] 랜덤 요금제 요청");

        Plan randomPlan = planRepository.findRandomPlanByTelecomCompany(
                randomPlanRequestDto.getTelecomCompany()
        ).orElseThrow(EmptyPlanException::new);

        log.info("[getRandomPlan] 랜덤 요금제 선택: planId={}, monthlyDataMb={}", randomPlan.getPlanId(), randomPlan.getMonthlyDataMb());

        return RandomPlanResponseDto.builder()
                .planId(randomPlan.getPlanId())
                .monthlyDataMb(randomPlan.getMonthlyDataMb())
                .build();
    }
}
