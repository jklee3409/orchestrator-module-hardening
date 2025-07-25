package eureca.capstone.project.orchestrator.auth.constant;

public class FilterConstant {
    // 화이트리스트 (인증 없이 접근 허용할 경로)
    public static final String[] WHITE_LIST = {
            "/healthCheck",

            "/orchestrator/user/",
            "/orchestrator/user/check-email",

            "/orchestrator/user/password-reset/**",


            "/orchestrator/auth/login",
            "/orchestrator/auth/verify-email",

            "/login/oauth2/code/kakao",
            "/oauth2/authorization/kakao",
            "/login/oauth2/code/google",
            "/oauth2/authorization/google",

            "/orchestrator/oauth/token",

            "/login/oauth2/code/**",
            "/oauth2/authorization/**",

            "/error",
            "/favicon.ico",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",

            "/orchestrator/webhook/toss"
    };
    // 블랙리스트 (인증 필요, 추가적인 제한 걸 경로 – 예: 관리자 등급)
    public static final String[] BLACK_LIST = {
            "/**"
    };
    // 리프레쉬 토큰 재발급 경로
    public static final String REFRESH_PATH = "/auth/reissue";

    public static final String[] PUBLIC_GET_URIS = {
            "/orchestrator/transaction-feed/search", // 판매글 목록 조회
            "/orchestrator/transaction-feed/{transactionFeedId:[0-9]+}", // 판매글 상세 조회 (숫자 ID만 허용)
            "/orchestrator/statistic", // 시세 통계 조회
            "/orchestrator/bid/{transactionFeedId:[0-9]+}", // 입찰 내역 조회
            "/orchestrator/recommend", // 추천 상품 조회
            "/orchestrator/recommend/related/{transactionFeedId:[0-9]+}", // 연관 상품 조회
    };
}
