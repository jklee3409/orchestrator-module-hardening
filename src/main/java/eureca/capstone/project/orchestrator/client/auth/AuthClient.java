package eureca.capstone.project.orchestrator.client.auth;

import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.request.auth.CryptoPasswordRequestDto;
import eureca.capstone.project.orchestrator.dto.response.auth.CryptoPasswordResponseDto;
import eureca.capstone.project.orchestrator.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClient {
    @Value("${services.auth.uri}")
    private String authServiceUri;
    private final WebClientUtil webClientUtil;

    public BaseResponseDto<CryptoPasswordResponseDto> cryptoPassword(CryptoPasswordRequestDto cryptoPasswordRequestDto) {
        log.info(authServiceUri + "/auth/crypto-password");
        return webClientUtil.post(
                authServiceUri + "/auth/crypto-password", // 이거 어디선거 한곳에서 관리해야할거 같음
                cryptoPasswordRequestDto,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
