package eureca.capstone.project.orchestrator.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Component
public class RewardSelector {

    /**
     * 사용자 ID와 날짜 기반으로 Seed를 고정한 확률형 보상 시스템
     *
     * @param userId 사용자 식별자
     * @return 추첨된 금액 (1 ~ 1,000,000원 중 확률 기반 선택)
     */
    public int selectTodayReward(Long userId) {
        long seed = Objects.hash(userId, LocalDate.now());
        Random random = new Random(seed);

        double rand = random.nextDouble() * 100;

        int reward;
        if (rand < 30.0) {
            reward = 1;             // 30%
        } else if (rand < 55.0) {
            reward = 100;           // 25%
        } else if (rand < 75.0) {
            reward = 1_000;         // 20%
        } else if (rand < 90.0) {
            reward = 10_000;        // 15%
        } else if (rand < 98.0) {
            reward = 100_000;       // 8%
        } else {
            reward = 1_000_000;     // 2%
        }

        log.info("[selectTodayReward] userId={}, rand={}, reward={}", userId, rand, reward);
        return reward;
    }
}