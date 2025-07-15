package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;

public interface PlanService {
    RandomPlanResponseDto getRandomPlan(RandomPlanRequestDto randomPlanRequestDto);
}
