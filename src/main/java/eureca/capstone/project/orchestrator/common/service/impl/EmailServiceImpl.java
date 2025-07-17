package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String to, String subject, String text) {
        // 요청값 로그 출력
        log.info("[sendEmail] 이메일 전송 전 입니다. to : {}, subject : {}, test : {}", to, subject, text);

        // 메일 객체 생성 및 셋팅
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        // 메일 발송
        javaMailSender.send(message);

        // 메서드 완료 로그 출력
        log.info("[sendEmail] 이메일이 정상적으로 전송되었습니다. to : {}, subject : {}, test : {}", to, subject, text);
    }
}
