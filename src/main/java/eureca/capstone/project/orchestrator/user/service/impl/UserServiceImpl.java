package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.OAuthRegistrationResultDto;
import eureca.capstone.project.orchestrator.auth.entity.UserRole;
import eureca.capstone.project.orchestrator.auth.repository.RoleRepository;
import eureca.capstone.project.orchestrator.auth.repository.UserRoleRepository;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.EmailAlreadyExistsException;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.PlanNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.TelecomCompanyNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.service.AIService;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.user.dto.PlanDto;
import eureca.capstone.project.orchestrator.user.dto.request.plan.RandomPlanRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateNicknameRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdatePasswordRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateUserTelecomAndPhoneRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.plan.RandomPlanResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.*;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.PlanRepository;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.PlanService;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import eureca.capstone.project.orchestrator.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final StatusManager statusManager;
    private final PlanService planService;
    private final UserDataService userDataService;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final TelecomCompanyRepository telecomCompanyRepository;
    private final PlanRepository planRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final AIService aiService;
    private final EmailVerificationService emailVerificationService;
    private final TransactionFeedSearchRepository transactionFeedSearchRepository;
    private final UserPayRepository userPayRepository;

    /**
     * 새로운 사용자를 생성합니다.
     * 이메일 중복 확인 후 사용자 정보를 저장하고, 사용자 역할을 부여합니다.
     * 또한 랜덤 요금제를 할당하고 사용자 데이터 레코드를 생성합니다.
     *
     * @param createUserRequestDto 사용자 생성에 필요한 정보(이메일, 비밀번호, 닉네임, 전화번호, 통신사 ID 등)
     * @return 생성된 사용자의 ID
     * @throws EmailAlreadyExistsException     이미 존재하는 이메일인 경우
     * @throws TelecomCompanyNotFoundException 존재하지 않는 통신사 ID인 경우
     * @throws InternalServerException         사용자 생성 중 오류 발생 시
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
                    .nickname(aiService.generateNickname())
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

            // UserPay 생성 및 저장
            UserPay userPay = new UserPay(savedUser);
            userPayRepository.save(userPay);
            log.info("[createUser] 신규 사용자 {}의 UserPay 생성 완료.", savedUser.getEmail());


            // 랜덤 요금제 조회
            RandomPlanRequestDto planReq = RandomPlanRequestDto.builder()
                    .telecomCompany(telecomCompany)
                    .build();
            RandomPlanResponseDto randomPlan = planService.getRandomPlan(planReq);

            Plan randomPlanEntity = planRepository.findById(randomPlan.getPlanId())
                    .orElseThrow(PlanNotFoundException::new);

            // 사용자 데이터 레코드 생성
            CreateUserDataRequestDto userDataReq = CreateUserDataRequestDto.builder()
                    .userId(savedUser.getUserId())
                    .plan(PlanDto.fromEntity(randomPlanEntity))
                    .monthlyDataMb(randomPlan.getMonthlyDataMb())
                    .resetDataAt(savedUser.getCreatedAt().getDayOfMonth())
                    .build();

            userDataService.createUserData(userDataReq);

            // 인증 이메일 발송
            emailVerificationService.sendVerificationEmail(createUserRequestDto.getEmail());

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

        transactionFeedSearchRepository.updateNicknameBySellerId(user.getUserId(), updateUserNicknameRequestDto.getNickname());

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

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(updatePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
            log.error("[updateUserPassword] 현재 비밀번호가 일치하지 않습니다.");
            throw new InternalServerException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(updatePasswordRequestDto.getNewPassword(), user.getPassword())) {
            throw new InternalServerException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        user.updateUserPassword(passwordEncoder.encode(updatePasswordRequestDto.getNewPassword()));
        log.info("[updateUserPassword] 사용자 {} 비밀번호 업데이트 완료", user.getEmail());

        return UpdatePasswordResponseDto.builder()
                .userId(user.getUserId())
                .build();
    }

    @Transactional
    @Override
    public OAuthRegistrationResultDto OAuthUserRegisterIfNotExists(String email, String provider) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        // 이미 사용자가 존재하면 ID를 반환하고, 신규 유저가 아님을 알림
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean isNewUser = user.getTelecomCompany() == null || user.getPhoneNumber() == null;
            log.info("[OAuthUserRegisterIfNotExists] 기존 사용자: {}. 추가 정보 필요 여부(isNewUser): {}", email, isNewUser);
            return new OAuthRegistrationResultDto(user.getUserId(), isNewUser);
        }

        // 시스템에 존재하지 않은 경우, 회원 가입 처리 및 권한 부여
        TelecomCompany randomTelecomCompany = telecomCompanyRepository.findRandomTelecomCompany();
        User savedUser = userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .nickname(aiService.generateNickname())
                        .provider(provider)
                        .telecomCompany(randomTelecomCompany)
                        .status(statusManager.getStatus("USER", "ACTIVE"))
                        .build()
        );

        // 역할과 권한 부여
        UserRole userRole = UserRole.builder()
                .user(savedUser)
                .role(roleRepository.findRoleByName("ROLE_USER"))
                .build();

        userRoleRepository.save(userRole);

        // UserPay 생성 및 저장
        UserPay userPay = new UserPay(savedUser);
        userPayRepository.save(userPay);
        log.info("[OAuthUserRegisterIfNotExists] 신규 사용자 {}의 UserPay 생성 완료.", email);

        // 신규 사용자의 ID를 반환하고, 신규 유저임을 알림
        return new OAuthRegistrationResultDto(savedUser.getUserId(), true);
    }

    @Override
    @Transactional
    public UpdateUserTelecomAndPhoneResponseDto updateUserTelecomAndPhone(String email, UpdateUserTelecomAndPhoneRequestDto requestDto) {
        log.info("[updateUserTelecomAndPhone] 사용자 {}의 통신사 및 전화번호 업데이트 요청", email);

        User user = findUserByEmail(email);
        TelecomCompany telecomCompany = telecomCompanyRepository.findById(requestDto.getTelecomCompanyId())
                .orElseThrow(TelecomCompanyNotFoundException::new);
        log.info("[updateUserTelecomAndPhone] 사용자 {}의 통신사 정보: {}", email, telecomCompany.getName());

        user.updateTelecomAndPhone(telecomCompany, requestDto.getPhoneNumber());
        log.info("[updateUserTelecomAndPhone] 사용자 {} 정보 업데이트 완료", email);

        if (!userDataRepository.existsByUserId(user.getUserId())) {
            log.info("[updateUserTelecomAndPhone] 사용자 {}의 요금제 정보가 없으므로 새로 할당합니다.", email);

            // 랜덤 요금제 조회
            RandomPlanRequestDto planReq = RandomPlanRequestDto.builder()
                    .telecomCompany(telecomCompany)
                    .build();
            RandomPlanResponseDto randomPlan = planService.getRandomPlan(planReq);

            Plan randomPlanEntity = planRepository.findById(randomPlan.getPlanId())
                    .orElseThrow(PlanNotFoundException::new);

            // 사용자 데이터 레코드 생성
            CreateUserDataRequestDto createUserDataRequestDto = CreateUserDataRequestDto.builder()
                    .userId(user.getUserId())
                    .plan(PlanDto.fromEntity(randomPlanEntity))
                    .monthlyDataMb(randomPlan.getMonthlyDataMb())
                    .resetDataAt(LocalDate.now().getDayOfMonth())
                    .build();

            userDataService.createUserData(createUserDataRequestDto);
            log.info("[updateUserTelecomAndPhone] 사용자 {}의 요금제 정보 할당 완료.", email);
        }

        return UpdateUserTelecomAndPhoneResponseDto.builder()
                .userId(user.getUserId())
                .telecomCompanyId(user.getTelecomCompany().getTelecomCompanyId())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private User findUserByEmail(String email) {
        log.info("email: {}", email);
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }
}
