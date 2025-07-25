package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.service.EmailService;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.*;
import static eureca.capstone.project.orchestrator.common.constant.UrlConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void requestPasswordReset(String email) {
        log.info("[requestPasswordReset] 비밀번호 재설정 요청: {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();
            String redisKey = "password-reset-token:" + token;

            // Redis에 토큰 저장 (15분 유효)
            redisService.setValue(redisKey, String.valueOf(user.getUserId()), 15, TimeUnit.MINUTES);

            // 이메일 발송
            String resetLink = RESET_PASSWORD_TOKEN_URL + token;
            String emailBody = String.format(PASSWORD_RESET_BODY, resetLink);
            emailService.sendEmail(email, PASSWORD_RESET_SUBJECT, emailBody);

        } else {
            log.warn("[requestPasswordReset] 존재하지 않는 이메일 주소로 비밀번호 재설정 요청: {}", email);
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        String redisKey = "password-reset-token:" + token;
        return redisService.hasKey(redisKey);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String redisKey = "password-reset-token:" + token;
        String userIdStr = (String) redisService.getValue(redisKey);

        if (userIdStr == null) {
            log.warn("[resetPassword] 유효하지 않거나 만료된 비밀번호 재설정 토큰: {}", token);
            throw new InternalServerException(ErrorCode.PASSWORD_RESET_LINK_EXPIRED);
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 새 비밀번호 암호화 및 업데이트
        user.updateUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user); // 변경사항 저장

        // 사용된 토큰 삭제
        redisService.deleteValue(redisKey);
        log.info("[resetPassword] 사용자 {} 비밀번호 재설정 완료 및 토큰 삭제.",
                user.getEmail());
    }

}
