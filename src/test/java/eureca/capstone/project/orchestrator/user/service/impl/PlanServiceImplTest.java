package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.EmptyPlanException;
import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import eureca.capstone.project.orchestrator.user.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanServiceImpl planService;

    private TelecomCompany telecomCompany;
    private Plan plan;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 객체 초기화
        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("테스트 통신사")
                .build();

        plan = Plan.builder()
                .planId(1L)
                .telecomCompany(telecomCompany)
                .planName("테스트 요금제")
                .monthlyDataMb(5000)
                .build();
    }

    @Test
    @DisplayName("통신사에 따른 랜덤 요금제 조회 성공")
    void getRandomPlan_Success() {
        // Given
        RandomPlanRequestDto requestDto = RandomPlanRequestDto.builder()
                .telecomCompany(telecomCompany)
                .build();
        
        when(planRepository.findRandomPlanByTelecomCompany(telecomCompany))
                .thenReturn(Optional.of(plan));

        // When
        RandomPlanResponseDto responseDto = planService.getRandomPlan(requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(plan.getPlanId(), responseDto.getPlanId());
        assertEquals(plan.getMonthlyDataMb(), responseDto.getMonthlyDataMb());
    }

    @Test
    @DisplayName("통신사에 등록된 요금제가 없는 경우 예외 발생")
    void getRandomPlan_EmptyPlan_ThrowsException() {
        // Given
        RandomPlanRequestDto requestDto = RandomPlanRequestDto.builder()
                .telecomCompany(telecomCompany)
                .build();
        
        when(planRepository.findRandomPlanByTelecomCompany(telecomCompany))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EmptyPlanException.class, () -> planService.getRandomPlan(requestDto));
    }
}