package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private final ChatClient chatClient;

    @Override
    public String generateNickname() {
        String nickName = chatClient.prompt()
                .user("닉네임 만들어줘")
                .call()
                .entity(String.class);
        log.info("[generateNickname] AI 를 통해 정상적으로 닉네임이 생성되었습니다. {}", nickName);

        return nickName;
    }

}
