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
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.GetUserProfileRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateNicknameRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdatePasswordRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserCountResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserProfileResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdateNicknameResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdatePasswordResponseDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.PlanService;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import eureca.capstone.project.orchestrator.user.service.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Override
    @Transactional(readOnly = true)
    public GetUserProfileResponseDto getUserProfile(GetUserProfileRequestDto getUserProfileRequestDto) {
        log.info("[getUserProfiles] 사용자 프로필 조회 요청");

        return GetUserProfileResponseDto.fromUser(
                userRepository.findById(getUserProfileRequestDto.getUserId()).orElseThrow(UserNotFoundException::new)
        );
    }

    @Override
    @Transactional
    public UpdateNicknameResponseDto updateUserNickname(UpdateNicknameRequestDto updateUserNicknameRequestDto) {
        log.info("[updateUserNickname] 사용자 {} 닉네입 업데이트", updateUserNicknameRequestDto.getUserId());

        Optional<User> optionalUser = userRepository.findById(updateUserNicknameRequestDto.getUserId());

        User user = optionalUser.orElseThrow(UserNotFoundException::new);
        user.updateUserNickname(updateUserNicknameRequestDto.getNickname());
        log.info("[updateUserNickname] 사용자 {} 닉네입 업데이트 완료. 변경된 닉네임: {}", updateUserNicknameRequestDto.getUserId(), user.getNickname());

        return UpdateNicknameResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    @Transactional
    public UpdatePasswordResponseDto updateUserPassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        log.info("[updateUserPassword] 비밀번호 업데이트 요청");

        User user = findUserForUpdatePassword(updatePasswordRequestDto);
        user.updateUserPassword(passwordEncoder.encode(updatePasswordRequestDto.getPassword()));

        return UpdatePasswordResponseDto.builder()
                .userId(user.getUserId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetUserCountResponseDto getUserCount() {
        log.info("[getUserCount] 전체 및 당일 가입자 수 조회 요청");

        Status activeStatus = statusManager.getStatus("USER", "ACTIVE");
        long totalUserCount = userRepository.countByStatus(activeStatus);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long todayUserCount = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        log.info("[getUserCount] 조회 완료 - 전체 활성 사용자: {}명, 당일 신규 가입자: {}명", totalUserCount, todayUserCount);

        return GetUserCountResponseDto.builder()
                .totalUserCount(totalUserCount)
                .todayUserCount(todayUserCount)
                .build();
    }

    private User findUserForUpdatePassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        if (updatePasswordRequestDto.getUserId() != null) {
            log.info("[findUserForUpdatePassword] ID 로 사용자 조회: {}", updatePasswordRequestDto.getUserId());
            return userRepository.findById(updatePasswordRequestDto.getUserId()).orElseThrow(UserNotFoundException::new);
        }
        else if (updatePasswordRequestDto.getEmail() != null) {
            log.info("[findUserForUpdatePassword] 이메일로 사용자 조회: {}", updatePasswordRequestDto.getEmail());
            return userRepository.findByEmail(updatePasswordRequestDto.getEmail())
                    .orElseThrow(UserNotFoundException::new);
        }
        else throw new InternalServerException(ErrorCode.INVALID_PARAMETER);
    }
}
