package eureca.capstone.project.orchestrator.service.user.impl;

import eureca.capstone.project.orchestrator.client.auth.AuthClient;
import eureca.capstone.project.orchestrator.client.user.UserClient;
import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.request.auth.CryptoPasswordRequestDto;
import eureca.capstone.project.orchestrator.dto.request.orchestrator.SignUpRequestDto;
import eureca.capstone.project.orchestrator.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.dto.request.user.UpdateUserPasswordRequestDto;
import eureca.capstone.project.orchestrator.dto.response.auth.CryptoPasswordResponseDto;
import eureca.capstone.project.orchestrator.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.dto.response.user.UpdateUserPasswordResponseDto;
import eureca.capstone.project.orchestrator.service.user.UserOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrchestrationServiceImpl implements UserOrchestrationService {
    private final UserClient userClient;
    private final AuthClient authClient;

    @Override
    public BaseResponseDto<CreateUserResponseDto> signup(SignUpRequestDto signUpRequestDto) {
        // 비밀번호 암호화
        CryptoPasswordRequestDto cryptoPasswordRequestDto = CryptoPasswordRequestDto.builder()
                .password(signUpRequestDto.getPassword())
                .build();
        BaseResponseDto<CryptoPasswordResponseDto> cryptoPassword = authClient.cryptoPassword(cryptoPasswordRequestDto);
        String password = cryptoPassword.getData().getCryptoPassword();
        log.info(password);

        // 암호화된 비밀번호로 회원가입
        CreateUserRequestDto createUserRequestDto = CreateUserRequestDto.builder().telecomCompany(signUpRequestDto.getTelecomCompany())
                .phone(signUpRequestDto.getPhoneNumber())
                .email(signUpRequestDto.getEmail())
                .password(password)
                .build();
        BaseResponseDto<CreateUserResponseDto> createUserResponseDto = userClient.createUser(createUserRequestDto);
        log.info(createUserResponseDto.toString());

        // 결과값 반환
        return createUserResponseDto;
    }

    @Override
    public BaseResponseDto<UpdateUserPasswordResponseDto> updateUserPassword(UpdateUserPasswordRequestDto updateUserPasswordRequestDto) {
        // 비밀번호 암호화
        CryptoPasswordRequestDto cryptoPasswordRequestDto = CryptoPasswordRequestDto.builder()
                .password(updateUserPasswordRequestDto.getPassword())
                .build();
        BaseResponseDto<CryptoPasswordResponseDto> cryptoPassword = authClient.cryptoPassword(cryptoPasswordRequestDto);
        String password = cryptoPassword.getData().getCryptoPassword();
        log.info(password);

        // 암호화된 비밀번호로 변경
        updateUserPasswordRequestDto.setPassword(password);
        BaseResponseDto<UpdateUserPasswordResponseDto> updateUserPasswordResponseDto = userClient.updateUserPassword(updateUserPasswordRequestDto);
        log.info(updateUserPasswordResponseDto.toString());

        // 결과값 반환
        return updateUserPasswordResponseDto;
    }
}
