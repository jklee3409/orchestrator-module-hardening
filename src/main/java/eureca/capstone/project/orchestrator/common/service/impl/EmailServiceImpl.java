package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Async
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        // 요청값 로그 출력
        log.info("[sendEmail] 이메일 전송 전 입니다. to : {}, subject : {}, test : {}", to, subject, body);

        try {
            // 단순 텍스트가 아닌, html 을 보낼수 있는 MimeMessage 객체 생성
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);

            // 객체에 받는사람 제목 내용을 포함 (html)
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            // 이메일 전송
            javaMailSender.send(message);
            log.info("[sendEmail] 이메일이 정상적으로 전송되었습니다.");
        } catch (MessagingException e) {
            // checked exception 처리
            e.printStackTrace();
            log.error(e.getMessage());
        }

        // 메서드 완료 로그 출력
        log.info("[sendEmail] 이메일이 정상적으로 전송되었습니다. to : {}, subject : {}, test : {}", to, subject, body);
    }
}
