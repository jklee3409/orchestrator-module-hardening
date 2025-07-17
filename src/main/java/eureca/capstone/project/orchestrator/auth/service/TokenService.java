package eureca.capstone.project.orchestrator.auth.service;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
    String generateToken(CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse);
}
