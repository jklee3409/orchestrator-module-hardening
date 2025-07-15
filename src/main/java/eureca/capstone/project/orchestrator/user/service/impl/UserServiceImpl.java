package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.auth.entity.UserRole;
import eureca.capstone.project.orchestrator.auth.repository.RoleRepository;
import eureca.capstone.project.orchestrator.auth.repository.UserRoleRepository;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.EmailAlreadyExistsException;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.TelecomCompanyNotFoundException;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.PlanService;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import eureca.capstone.project.orchestrator.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final StatusManager statusManager;
    private final PlanService planService;
    private final UserDataService userDataService;
    private final UserRepository userRepository;
    private final TelecomCompanyRepository telecomCompanyRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public CreateUserResponseDto createUser(CreateUserRequestDto createUserRequestDto) {
        log.info("[createUser] 사용자 등록 요청");

        Optional<User> optionalUser = userRepository.findByEmail(createUserRequestDto.getEmail());
        if (optionalUser.isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        try {
            TelecomCompany telecomCompany = telecomCompanyRepository.findById(
                    createUserRequestDto.getTelecomCompanyId()).orElseThrow(TelecomCompanyNotFoundException::new);

            Status pendingStatus = statusManager.getStatus("USER", "EMAIL_VERIFICATION_PENDING");

            log.info("[createUser] 사용자 등록 시작: {}", createUserRequestDto.getEmail());
            User user = User.builder()
                    .email(createUserRequestDto.getEmail())
                    .password(passwordEncoder.encode(createUserRequestDto.getPassword()))
                    .nickname(createUserRequestDto.getNickname())
                    .phoneNumber(createUserRequestDto.getPhoneNumber())
                    .provider(createUserRequestDto.getProvider())
                    .telecomCompany(telecomCompany)
                    .status(pendingStatus)
                    .build();
            User savedUser = userRepository.save(user);
            log.info("[createUser] 사용자 등록 완료: {}", user.getEmail());

            // 역할과 권한 부여

            UserRole userRole = UserRole.builder()
                    .user(savedUser)
                    .role(roleRepository.findRoleByName("ROLE_USER"))
                    .build();

            userRoleRepository.save(userRole);


            // 랜덤 요금제 조회
            RandomPlanRequestDto planReq = RandomPlanRequestDto.builder()
                    .telecomCompany(telecomCompany)
                    .build();
            RandomPlanResponseDto randomPlan = planService.getRandomPlan(planReq);

            // 사용자 데이터 레코드 생성
            CreateUserDataRequestDto userDataReq = CreateUserDataRequestDto.builder()
                    .userId(savedUser.getUserId())
                    .planId(randomPlan.getPlanId())
                    .monthlyDataMb(randomPlan.getMonthlyDataMb())
                    .resetDataAt(savedUser.getCreatedAt().getDayOfMonth())
                    .build();

            userDataService.createUserData(userDataReq);

            return CreateUserResponseDto.builder()
                    .id(user.getUserId())
                    .build();

        } catch (Exception e) {
            log.error("[createUser] 사용자 등록 중 오류 발생: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorCode.USER_CREATE_FAIL);
        }
    }
}
