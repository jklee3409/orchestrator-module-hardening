package eureca.capstone.project.orchestrator.alarm.repository.impl;

import static eureca.capstone.project.orchestrator.alarm.entity.QAlarm.alarm;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import eureca.capstone.project.orchestrator.alarm.entity.Alarm;
import eureca.capstone.project.orchestrator.alarm.repository.custom.AlarmRepositoryCustom;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AlarmRepositoryImpl implements AlarmRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final StatusManager statusManager;

    @Override
    @Transactional(readOnly = true)
    public Slice<NotificationDto> getAlarms(Long userId, Pageable pageable) {
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);

        List<Alarm> alarms = queryFactory
            .selectFrom(alarm)
            .join(alarm.user).fetchJoin()
            .join(alarm.alarmType).fetchJoin()
            .join(alarm.status).fetchJoin()
            .where(
                alarm.user.userId.eq(userId),
                alarm.createdAt.after(fourteenDaysAgo)
            )
            .orderBy(alarm.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = false;

        if (alarms.size() > pageable.getPageSize()) {
            alarms.remove(pageable.getPageSize());
            hasNext = true;
        }

        List<Long> transactionFeedIds = alarms.stream()
            .map(Alarm::getTransactionFeedId)
            .collect(Collectors.toList());

        QTransactionFeed transactionFeed = QTransactionFeed.transactionFeed;
        Map<Long, TransactionFeed> transactionFeedMap = queryFactory
            .selectFrom(transactionFeed)
            .where(transactionFeed.transactionFeedId.in(transactionFeedIds))
            .fetch().stream()
            .collect(Collectors.toMap(TransactionFeed::getTransactionFeedId, tf -> tf));

        List<NotificationDto> content = alarms.stream()
            .map(a -> NotificationDto.fromEntity(a, transactionFeedMap.get(a.getTransactionFeedId())))
            .collect(Collectors.toList());

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    @Transactional
    public void readAlarms(List<Long> alarmIds, Long userId) {
        if (alarmIds == null || alarmIds.isEmpty()) {
            return;
        }

        queryFactory
            .update(alarm)
            .set(alarm.status, statusManager.getStatus("ALARM", "READ"))
            .where(
                alarm.alarmId.in(alarmIds),
                alarm.user.userId.eq(userId)
            )
            .execute();
    }
}
