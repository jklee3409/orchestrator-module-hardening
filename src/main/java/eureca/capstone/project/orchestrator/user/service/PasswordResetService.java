package eureca.capstone.project.orchestrator.user.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    boolean isTokenValid(String token);
    void resetPassword(String token, String newPassword);
}
