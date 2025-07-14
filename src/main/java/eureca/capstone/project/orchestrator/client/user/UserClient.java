package eureca.capstone.project.orchestrator.client.user;

import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.dto.request.user.UpdateUserPasswordRequestDto;
import eureca.capstone.project.orchestrator.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.dto.response.user.UpdateUserPasswordResponseDto;
import eureca.capstone.project.orchestrator.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserClient {
    @Value("${services.user.uri}")
    private String userServiceUri;
    private final WebClientUtil webClientUtil;

    public BaseResponseDto<CreateUserResponseDto> createUser(CreateUserRequestDto createUserRequestDto) {
        return webClientUtil.post(
                userServiceUri + "/user/", // 이거 어디선거 한곳에서 관리해야할거 같음
                createUserRequestDto,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public BaseResponseDto<UpdateUserPasswordResponseDto> updateUserPassword(
            UpdateUserPasswordRequestDto updateUserPasswordRequestDto) {
        return webClientUtil.put(
                userServiceUri + "/user/password",
                updateUserPasswordRequestDto,
                new ParameterizedTypeReference<>(){
                }
        );
    }
}
