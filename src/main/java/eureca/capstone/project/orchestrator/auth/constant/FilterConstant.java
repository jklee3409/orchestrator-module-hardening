package eureca.capstone.project.orchestrator.auth.constant;

public class FilterConstant {
    // 화이트리스트 (인증 없이 접근 허용할 경로)
    public static final String[] whiteList = {
            "/healthCheck",
            "/user/",
            "/auth/login",

            "/auth/crypto-password",
            "/auth/generateToken",

            "/error",
            "/favicon.ico",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
    // 블랙리스트 (인증 필요, 추가적인 제한 걸 경로 – 예: 관리자 등급)
    public static final String[] blackList = {
            "/**"
    };
    // 리프레쉬 토큰 재발급 경로
    public static final String REFRESH_PATH = "/auth/reissue";
}
