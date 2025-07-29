package eureca.capstone.project.orchestrator.auth.constant;

public class TokenConstant {
    // 3분 (ms)
    public static final long ACCESS_TOKEN_VALIDITY = 3 * 60 * 1000L;
    // 2주 (ms)
    public static final long REFRESH_TOKEN_VALIDITY = 14 * 24 * 60 * 60 * 1000L;
    // (second)
    public static final int REFRESH_TOKEN_MAX_AGE_SEC = (int) (REFRESH_TOKEN_VALIDITY / 1000);
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
}
