package eureca.capstone.project.orchestrator.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    private final ChatClient chatClient;

    public String generateNicknameBy() {
        return chatClient.prompt()
                .user("닉네임 만들어줘")
                .call()
                .entity(String.class);
    }

}
