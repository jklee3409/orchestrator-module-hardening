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

    /**
     * 새로운 사용자를 생성합니다.
     * 이메일 중복 확인 후 사용자 정보를 저장하고, 사용자 역할을 부여합니다.
     * 또한 랜덤 요금제를 할당하고 사용자 데이터 레코드를 생성합니다.
     *
     * @param createUserRequestDto 사용자 생성에 필요한 정보(이메일, 비밀번호, 닉네임, 전화번호, 통신사 ID 등)
     * @return 생성된 사용자의 ID
     * @throws EmailAlreadyExistsException 이미 존재하는 이메일인 경우
     * @throws TelecomCompanyNotFoundException 존재하지 않는 통신사 ID인 경우
     * @throws InternalServerException 사용자 생성 중 오류 발생 시
     */
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

    /**
     * 사용자 프로필 정보를 조회합니다.
     * 사용자 ID를 기반으로 사용자 정보를 조회하여 프로필 정보를 반환합니다.
     *
     * @param email 조회할 사용자의 email
     * @return 사용자 프로필 정보 (이메일, 닉네임, 전화번호, 통신사)
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public GetUserProfileResponseDto getUserProfile(String email) {
        log.info("[getUserProfiles] 사용자 프로필 조회 요청");
        User user = findUserByEmail(email);
        return GetUserProfileResponseDto.fromUser(user);
    }

    /**
     * 사용자의 닉네임을 업데이트합니다.
     * 사용자 ID를 기반으로 사용자를 찾아 새로운 닉네임으로 업데이트합니다.
     *
     * @param updateUserNicknameRequestDto 새로운 닉네임
     * @return 업데이트된 사용자 ID와 닉네임
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public UpdateNicknameResponseDto updateUserNickname(String email, UpdateNicknameRequestDto updateUserNicknameRequestDto) {
        log.info("[updateUserNickname] 사용자 {} 닉네입 업데이트", email);

        User user = findUserByEmail(email);
        user.updateUserNickname(updateUserNicknameRequestDto.getNickname());
        log.info("[updateUserNickname] 사용자 {} 닉네입 업데이트 완료. 변경된 닉네임: {}", email, user.getNickname());

        return UpdateNicknameResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    }

    /**
     * 사용자의 비밀번호를 업데이트합니다.
     * 사용자 ID 또는 이메일을 기반으로 사용자를 찾아 새로운 비밀번호로 업데이트합니다.
     * 비밀번호는 암호화되어 저장됩니다.
     *
     * @param updatePasswordRequestDto 사용자 ID 또는 이메일과 새로운 비밀번호
     * @return 업데이트된 사용자 ID
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public UpdatePasswordResponseDto updateUserPassword(String email, UpdatePasswordRequestDto updatePasswordRequestDto) {
        log.info("[updateUserPassword] 비밀번호 업데이트 요청");

        User user = findUserByEmail(email);
        user.updateUserPassword(passwordEncoder.encode(updatePasswordRequestDto.getPassword()));

        return UpdatePasswordResponseDto.builder()
                .userId(user.getUserId())
                .build();
    }

    /**
     * 전체 활성 사용자 수와 당일 가입한 사용자 수를 조회합니다.
     * 활성 상태인 사용자의 총 수와 오늘 생성된 사용자의 수를 계산합니다.
     *
     * @return 전체 활성 사용자 수와 당일 가입자 수
     */
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

    private User findUserByEmail(String email) {
        log.info("email: {}", email);
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }
}
