package eureca.capstone.project.orchestrator.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.orchestrator.common.dto.KeywordRankingDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.SEARCH_RANKING_KEY;

;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        log.info("[RedisService] 초기화: 랭킹 스냅샷 스케줄러를 시작합니다.");
        scheduler.scheduleAtFixedRate(
                () -> updatePreviousRankingSnapshot(10),
                10,
                10,
                TimeUnit.MINUTES
        );
    }

    @PreDestroy
    public void destroy() {
        log.info("RedisService 소멸: 스케줄러를 종료합니다.");
        scheduler.shutdown();
    }

    /**
     * 값을 저장 (TTL 없음)
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 값을 저장 (TTL 설정)
     */
    public void setValue(String key, Object value, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
    }

    /**
     * TTL 설정된 Duration 기반 저장
     */
    public void setValue(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * 값 조회
     */
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 값 삭제
     */
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    /**
     * TTL 조회
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 키 존재 여부
     */
    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    /**
     * 검색어를 ZSET에 1점씩 증가시킵니다.
     */
    public void increaseSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return;
        keyword = sanitizeKeyword(keyword);
        redisTemplate.opsForZSet().incrementScore(SEARCH_RANKING_KEY, keyword.toLowerCase(), 1);
    }

    public String sanitizeKeyword(String rawKeyword) {
        return rawKeyword
                .replaceAll("\\p{Cntrl}", "")   // 제어문자 제거 (\b 포함)
                .trim()
                .toLowerCase();                 // 필요 시 소문자 정규화
    }

    /**
     * 인기 검색어 Top N 조회 (예: Top 10)
     */
    public List<Object> getTopSearchKeywords(int topN) {
        Set<Object> keywords = redisTemplate.opsForZSet()
                .reverseRange(SEARCH_RANKING_KEY, 0, topN - 1);

        return keywords == null ? List.of() : new ArrayList<>(keywords);
    }

    /**
     * 인기 없는 검색어 하위 N개 제거
     */
    public void trimLowRankedKeywords(int keepTopN) {
        redisTemplate.opsForZSet().removeRange(SEARCH_RANKING_KEY, keepTopN, -1);
    }

    /**
     * 전체 초기화 (관리용)
     */
    public void clearAll() {
        redisTemplate.delete(SEARCH_RANKING_KEY);
    }

    public List<KeywordRankingDto> getTopSearchKeywordsWithTrend(int topN) {

        /* 1. 현재 TopN 조회 */
        Set<Object> currentSet = redisTemplate.opsForZSet()
                .reverseRange(SEARCH_RANKING_KEY, 0, topN - 1);
        List<String> current = currentSet == null ? List.of()
                : currentSet.stream().map(Object::toString).toList();

        /* 2. 이전 랭킹 JSON 문자열 안전하게 파싱 */
        List<String> prev = getPreviousRanking();

        /* 3. 현재·이전 비교 → DTO 생성 */
        List<KeywordRankingDto> result = new ArrayList<>();
        for (int i = 0; i < current.size(); i++) {
            String kw = current.get(i);
            int prevIdx = prev.indexOf(kw);

            String trend;
            Integer gap = null;

            if (prevIdx == -1) {
                trend = "NEW";
            } else if (prevIdx > i) {
                trend = "UP";
                gap = prevIdx - i;
            } else if (prevIdx < i) {
                trend = "DOWN";
                gap = i - prevIdx;
            } else {
                trend = "SAME";
                gap = 0;
            }
            result.add(new KeywordRankingDto(kw, i + 1, trend, gap));
        }

        return result;
    }

    private List<String> getPreviousRanking() {
        Object rawPrev = redisTemplate.opsForValue().get(SEARCH_RANKING_KEY + ":prev");
        if (rawPrev instanceof String json) {
            try {
                return objectMapper.readValue(json, new TypeReference<>() {}
                );
            } catch (Exception e) {
                log.warn("이전 랭킹 파싱 실패. 빈 리스트를 반환합니다.", e);
                return List.of();
            }
        }
        return List.of();
    }

    private void updatePreviousRankingSnapshot(int topN) {
        Set<Object> currentSet = redisTemplate.opsForZSet()
                .reverseRange(SEARCH_RANKING_KEY, 0, topN - 1);
        List<String> current = currentSet == null ? List.of()
                : currentSet.stream().map(Object::toString).toList();

        try {
            String json = objectMapper.writeValueAsString(current);
            redisTemplate.opsForValue().set(SEARCH_RANKING_KEY + ":prev", json);
            log.info("실시간 검색어 랭킹 스냅샷을 갱신했습니다.");
        } catch (JsonProcessingException e) {
            log.error("랭킹 스냅샷 JSON 변환 중 오류가 발생했습니다.", e);
        }
    }
}
