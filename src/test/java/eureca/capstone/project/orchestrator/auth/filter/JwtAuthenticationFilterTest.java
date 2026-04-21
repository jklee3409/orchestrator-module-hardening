package eureca.capstone.project.orchestrator.auth.filter;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.config.properties.JmeterBypassProperties;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TEST_KEY = "local-bid-hotpath";
    private static final String BIDDER_EMAIL = "bidbench-redis-bidder0001@loadtest.local";

    @Mock private JwtUtil jwtUtil;
    @Mock private CookieUtil cookieUtil;
    @Mock private RedisService redisService;
    @Mock private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                jwtUtil,
                cookieUtil,
                redisService,
                userDetailsService,
                new JmeterBypassProperties(true, TEST_KEY)
        );
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JMeter bypass headers should authenticate with the CSV email without parsing JWT")
    void shouldAuthenticateWhenJmeterBypassHeadersMatch() throws Exception {
        CustomUserDetailsDto userDetails = CustomUserDetailsDto.builder()
                .userId(1L)
                .email(BIDDER_EMAIL)
                .password("ignored")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("TRANSACTION")
                ))
                .build();
        when(userDetailsService.loadUserByUsername(BIDDER_EMAIL)).thenReturn(userDetails);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orchestrator/bid");
        request.addHeader("X-JMeter-Test-Key", TEST_KEY);
        request.addHeader("Authorization", "Bearer " + BIDDER_EMAIL);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(BIDDER_EMAIL);
        assertThat(authentication.getAuthorities())
                .extracting(grantedAuthority -> grantedAuthority.getAuthority())
                .contains("ROLE_USER", "TRANSACTION");

        verify(userDetailsService).loadUserByUsername(BIDDER_EMAIL);
        verifyNoInteractions(jwtUtil);
    }
}
