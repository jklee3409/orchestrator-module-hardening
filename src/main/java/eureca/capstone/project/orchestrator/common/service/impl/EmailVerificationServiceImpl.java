package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.config.properties.AppUrlProperties;
import eureca.capstone.project.orchestrator.common.exception.custom.EmailVerifyTokenMismatchException;
import eureca.capstone.project.orchestrator.common.service.EmailService;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.EMAIL_BODY;
import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.EMAIL_SUBJECT;
import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_PENDING_EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final AppUrlProperties appUrlProperties;
    private final EmailService emailService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final StatusManager statusManager;

    @Override
    public void sendVerificationEmail(String email) {
        log.info("[sendVerificationEmail] {}", email);
        String token = UUID.randomUUID().toString();
        String redisKey = REDIS_PENDING_EMAIL + token;
        redisService.setValue(redisKey, email, Duration.ofMinutes(30));

        String verificationLink = appUrlProperties.verifyEmailBase() + token;
        String emailBody = EMAIL_BODY.formatted(verificationLink);
        emailService.sendEmail(email, EMAIL_SUBJECT, emailBody);
    }

    @Override
    @Transactional
    public void verifyEmailToken(String token) {
        String redisKey = REDIS_PENDING_EMAIL + token;
        String email = (String) redisService.getValue(redisKey);

        if (email == null) {
            throw new EmailVerifyTokenMismatchException();
        }

        Long updateCount = userRepository.updateStatusByEmail(
                email,
                statusManager.getStatus("USER", "ACTIVE")
        );
        log.info("updateCount : {}", updateCount);

        redisService.deleteValue(redisKey);
    }
}
