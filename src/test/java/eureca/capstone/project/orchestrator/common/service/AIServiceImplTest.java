package eureca.capstone.project.orchestrator.common.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class AIServiceImplTest {
    @Autowired
    AIService aiService;

    @Test
    public void createNickName() {
        // given
        // 별도의 사전 조건은 없음 (AIService 주입 상태)

        // when
        String nickname = aiService.generateNicknameBy();

        // then
        assertThat(nickname)
                .isNotNull()
                .isNotBlank()
                .hasSizeBetween(1, 12);
    }
}
