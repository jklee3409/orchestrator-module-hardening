package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.EmailVerifyTokenMismatchException;
import eureca.capstone.project.orchestrator.common.service.EmailService;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.EMAIL_BODY;
import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.EMAIL_SUBJECT;
import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_PENDING_EMAIL;
import static eureca.capstone.project.orchestrator.common.constant.UrlConstant.VERIFY_EMAIL_TOKEN_URL;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
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

        String verificationLink = VERIFY_EMAIL_TOKEN_URL + token;
        String emailBody = EMAIL_BODY.formatted(verificationLink);

        emailService.sendEmail(email, EMAIL_SUBJECT, emailBody);
    }

    @Override
    public void verifyEmailToken(String token) {
        // 키값을 통해 레디스에 인증 대기중인 토큰 값이 존재하는지 확인
        String redisKey = REDIS_PENDING_EMAIL + token;
        String email = (String) redisService.getValue(redisKey);

        // 만약 해당 키값을 통한 email 이 존재하지 않는다면, Exception 처리
        if (email == null) throw new EmailVerifyTokenMismatchException();

        // 상태값 변경
        Long updateCount = userRepository.updateStatusByEmail(
                email,
                statusManager.getStatus("USER", "ACTIVE")
        );
        log.info("updateCount : {}", updateCount);

        // 인증 완료 후 토큰 삭제
        redisService.deleteValue(redisKey);
    }
}
