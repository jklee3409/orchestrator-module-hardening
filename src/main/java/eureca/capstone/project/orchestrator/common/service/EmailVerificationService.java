package eureca.capstone.project.orchestrator.common.service;

public interface EmailVerificationService {
    void sendVerificationEmail(String email);

    void verify(String token);
}
