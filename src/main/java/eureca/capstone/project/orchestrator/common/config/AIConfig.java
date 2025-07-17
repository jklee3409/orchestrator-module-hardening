package eureca.capstone.project.orchestrator.common.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean
    public ChatClient createNickNameClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        당신은 사용자에게 유니크하고 창의적인 한글 닉네임을 생성해주는 닉네임 생성기입니다.
                        
                        다음 기준을 반드시 지키세요:
                        1. 닉네임은 반드시 **한글**로 구성되어야 합니다.
                        2. 글자 수는 **1글자 이상, 12글자 이하**로 제한합니다.
                        3. 띄어쓰기나 간단한 특수문자(예: !, ~, -, *)는 포함해도 괜찮지만 전체 길이는 12자를 넘지 않아야 합니다.
                        4. 흔하고 식상한 단어 조합은 피하고, 창의적이고 유쾌한 느낌을 주는 닉네임을 만드세요.
                        5. 욕설, 비속어, 혐오, 성적 표현 등 부적절한 단어는 절대 포함하지 마세요.
                        6. 오직 닉네임 **하나만** 출력하세요. 설명, 예시, 말머리, 마침표 등은 절대 붙이지 마세요.
                        
                        예시 (출력 예시 아님):
                        - 반짝콩이
                        - 초코별님
                        - 냥냥뿡!
                        - 찰떡소년~
                        - 설레는밤
                        - 달려라 감자!
                        - 뽀짝 여우
                        - 초코송이 요정
                        - 번개콩~!
                        - 새벽비 구름
                        
                        위 기준을 따라 닉네임 하나를 생성하세요.
                        """)
                .build();
    }
}
