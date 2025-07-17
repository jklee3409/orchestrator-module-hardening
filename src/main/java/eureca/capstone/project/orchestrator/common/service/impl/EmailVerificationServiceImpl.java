package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.EmailVerifyTokenMismatchException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.service.EmailService;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.RedisPendingEmail;


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
        String redisKey = RedisPendingEmail + token;
        redisService.setValue(redisKey, email, Duration.ofMinutes(30));

        String verificationLink = "https://www.visiblego.com/orchestrator/verify-email?token=" + token;
        String emailBody = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>Datcha 이메일 인증</title>
                </head>
                <body style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 40px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                        <h2 style="color: #5A3EEC;">Datcha 회원가입 인증</h2>
                        <p>안녕하세요, <strong>Datcha</strong>에 가입해주셔서 감사합니다.</p>
                        <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요:</p>
                        <div style="text-align: left; margin: 30px 0;">
                            <a href="%s"
                               style="background-color: #5A3EEC; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px;">
                                이메일 인증하기
                            </a>
                        </div>
                        <p style="font-size: 12px; color: #888;">이 메일은 발신 전용입니다.</p>
                    </div>
                </body>
                </html>
                """.formatted(verificationLink);

        emailService.sendEmail(email, "Datcha 회원가입 인증 메일 입니다.", emailBody);
    }

    @Transactional
    @Override
    public void verifyEmailToken(String token) {
        // 키값을 통해 레디스에 인증 대기중인 토큰 값이 존재하는지 확인
        String redisKey = RedisPendingEmail + token;
        String email = (String) redisService.getValue(redisKey);

        // 만약 해당 키값을 통한 email 이 존재하지 않는다면, Exception 처리
        if (email == null) throw new EmailVerifyTokenMismatchException();

        // 상태값 변경
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        user.setStatus(statusManager.getStatus("USER", "ACTIVE"));

        // 인증 완료 후 토큰 삭제
        redisService.deleteValue(redisKey);
    }
}
