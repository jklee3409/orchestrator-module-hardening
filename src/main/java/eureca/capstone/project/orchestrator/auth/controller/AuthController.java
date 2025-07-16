package eureca.capstone.project.orchestrator.auth.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.dto.request.LoginRequestDto;
import eureca.capstone.project.orchestrator.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.orchestrator.auth.service.TokenService;
import eureca.capstone.project.orchestrator.common.constant.RedisConstant;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.exception.custom.BlackListUserException;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.RedisBlackListUser;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisService redisService;

    @PostMapping("/login")
    public BaseResponseDto<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        // 사용자 정보 검증 및 로그 출력
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );
        log.info("authentication: {}", authentication);

        // JWT 토큰 발급 (AccessToken, RefreshToken)
        String accessToken = tokenService.generateToken(
                loginRequestDto,
                (CustomUserDetailsDto) authentication.getPrincipal(),
                httpServletResponse
        );

        // 리턴 객체 생성 및 로그 출력
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .build();
        BaseResponseDto<LoginResponseDto> success = BaseResponseDto.success(loginResponseDto);
        log.info("success: {}", success);

        // return
        return success;
    }
}
