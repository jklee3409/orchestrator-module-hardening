package eureca.capstone.project.orchestrator.auth.service;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.dto.request.LoginRequestDto;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
    String generateToken(LoginRequestDto loginRequestDto, CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse);
}
