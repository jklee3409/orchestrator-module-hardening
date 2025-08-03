package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.service.QuizAiService;
import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuizAiServiceImpl implements QuizAiService {
    private final ChatClient chatClient;

    public QuizAiServiceImpl(@Qualifier("quizClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public GetTodayQuizResponseDto generateQuiz() {
        GetTodayQuizResponseDto quiz = chatClient.prompt()
                .user("재밌는 넌센스 문제 잘 만들어줘")
                .call()
                .entity(GetTodayQuizResponseDto.class);
        log.info("[generateQuiz] AI 를 통해 정상적으로 퀴즈가 생성되었습니다. {}", quiz);
        return quiz;
    }
}
