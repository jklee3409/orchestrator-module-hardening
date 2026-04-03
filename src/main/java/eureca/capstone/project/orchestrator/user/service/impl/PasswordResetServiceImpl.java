package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.config.properties.AppUrlProperties;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.service.EmailService;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.PasswordResetService;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.PASSWORD_RESET_BODY;
import static eureca.capstone.project.orchestrator.common.constant.EmailConstant.PASSWORD_RESET_SUBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private final AppUrlProperties appUrlProperties;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void requestPasswordReset(String email) {
        log.info("[requestPasswordReset] {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.warn("[requestPasswordReset] user not found for email={}", email);
            return;
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        String redisKey = "password-reset-token:" + token;
        redisService.setValue(redisKey, String.valueOf(user.getUserId()), 15, TimeUnit.MINUTES);

        String resetLink = appUrlProperties.resetPasswordBase() + token;
        String emailBody = String.format(PASSWORD_RESET_BODY, resetLink);
        emailService.sendEmail(email, PASSWORD_RESET_SUBJECT, emailBody);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String redisKey = "password-reset-token:" + token;
        String userIdStr = (String) redisService.getValue(redisKey);

        if (userIdStr == null) {
            log.warn("[resetPassword] expired token={}", token);
            throw new InternalServerException(ErrorCode.PASSWORD_RESET_LINK_EXPIRED);
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redisService.deleteValue(redisKey);
    }
}
