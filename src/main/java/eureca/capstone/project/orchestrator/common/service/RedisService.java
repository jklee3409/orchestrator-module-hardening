package eureca.capstone.project.orchestrator.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.SEARCH_RANKING_KEY;

;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

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
}
