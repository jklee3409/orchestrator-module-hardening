package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.service.EmailService;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.RedisPendingEmail;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final EmailService emailService;
    private final RedisService redisService;

    @Override
    public void sendVerificationEmail(String email) {
        log.info("[sendVerificationEmail] {}", email);
        String token = UUID.randomUUID().toString();
        String redisKey = RedisPendingEmail + token;
        redisService.setValue(redisKey, email, Duration.ofMinutes(30));

        String verificationLink = "https://www.visiblego.com/auth/verify-email?token=" + token;
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
                        <p style="font-size: 14px; color: #555;">버튼이 작동하지 않으면 아래 링크를 복사하여 브라우저에 붙여넣어주세요:</p>
                        <p style="font-size: 14px; color: #555;"><a href="%s">%s</a></p>
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                        <p style="font-size: 12px; color: #888;">이 메일은 발신 전용입니다. 문의사항은 datcha-support@yourdomain.com 으로 보내주세요.</p>
                    </div>
                </body>
                </html>
                """.formatted(verificationLink, verificationLink, verificationLink);
        emailService.sendEmail(email, "Datcha 회원가입 인증 메일 입니다.", emailBody);
    }

    @Override
    public void verify(String token) {

    }
}
