package eureca.capstone.project.orchestrator.common.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailVerificationServiceTest {
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    public void sendVerificationEmailTest() {
        // given
        String email = "sbi1024@naver.com";

        // when
        emailVerificationService.sendVerificationEmail(email);

        // then

    }
}
