package eureca.capstone.project.orchestrator.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.orchestrator.common.dto.GetRankingResponseDto;
import eureca.capstone.project.orchestrator.common.dto.KeywordRankingDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
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

    private static final DateTimeFormatter KEY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
    private static final DateTimeFormatter UPDATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String generateRankingKey(LocalDateTime dateTime) {
        String timeBlock = dateTime.getMinute() < 30 ? "00" : "30";
        return "search_ranking:" + dateTime.format(KEY_DATE_FORMATTER) + "_" + timeBlock;
    }

    private String getCurrentRankingKey() {
        return generateRankingKey(LocalDateTime.now());
    }

    private String getPreviousRankingKey() {
        return generateRankingKey(LocalDateTime.now().minusMinutes(30));
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
        String key = getCurrentRankingKey();

        redisTemplate.opsForZSet().incrementScore(key, keyword.toLowerCase(), 1);
        if (redisTemplate.getExpire(key) == null || redisTemplate.getExpire(key) < 0) {
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
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

    public GetRankingResponseDto getTopSearchKeywordsWithTrend(int topN) {
        LocalDateTime now = LocalDateTime.now();
        String currentKey = getCurrentRankingKey();
        String prevKey = getPreviousRankingKey();

        // 1. 임시 키 생성
        String tempKey = "temp_ranking:" + UUID.randomUUID().toString();

        try {
            // 2. 이전 키와 현재 키를 합산하여 임시 키에 저장
            // 동일 멤버는 점수가 합산됨
            redisTemplate.opsForZSet().unionAndStore(prevKey, Collections.singletonList(currentKey), tempKey);

            // 3. 임시 키에서 최종 TopN 조회
            Set<Object> currentSet = redisTemplate.opsForZSet().reverseRange(tempKey, 0, topN - 1);
            List<String> currentHybridRank = (currentSet == null) ? List.of() : currentSet.stream().map(Object::toString).toList();

            // 4. 순위 비교 기준은 '이전 시간대의 랭킹'으로 설정
            Set<Object> prevSet = redisTemplate.opsForZSet().reverseRange(prevKey, 0, topN - 1);
            List<String> prevRank = (prevSet == null) ? List.of() : prevSet.stream().map(Object::toString).toList();

            // 5. DTO 생성
            List<KeywordRankingDto> rankingList = new ArrayList<>();
            for (int i = 0; i < currentHybridRank.size(); i++) {
                String kw = currentHybridRank.get(i);
                int prevIdx = prevRank.indexOf(kw);
                String trend;
                Integer gap = null;

                if (prevIdx == -1) trend = "NEW";
                else if (prevIdx > i) { trend = "UP"; gap = prevIdx - i; }
                else if (prevIdx < i) { trend = "DOWN"; gap = i - prevIdx; }
                else { trend = "SAME"; gap = 0; }

                rankingList.add(new KeywordRankingDto(kw, i + 1, trend, gap));
            }

            // 6. 갱신 시각 생성 및 최종 DTO 반환
            String lastUpdatedAt = now.withMinute(now.getMinute() < 30 ? 0 : 30).format(UPDATE_TIME_FORMATTER);
            return GetRankingResponseDto.builder()
                    .lastUpdatedAt(lastUpdatedAt)
                    .top10(rankingList)
                    .build();

        } finally {
            // 7. 임시 키 삭제
            redisTemplate.delete(tempKey);
        }
    }
}
