package eureca.capstone.project.orchestrator.common.component;

import eureca.capstone.project.orchestrator.alarm.entity.AlarmType;
import eureca.capstone.project.orchestrator.alarm.repository.AlarmTypeRepository;
import eureca.capstone.project.orchestrator.auth.entity.Authority;
import eureca.capstone.project.orchestrator.auth.entity.Role;
import eureca.capstone.project.orchestrator.auth.entity.RoleAuthority;
import eureca.capstone.project.orchestrator.auth.repository.AuthorityRepository;
import eureca.capstone.project.orchestrator.auth.repository.RoleAuthorityRepository;
import eureca.capstone.project.orchestrator.auth.repository.RoleRepository;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.repository.StatusRepository;
import eureca.capstone.project.orchestrator.pay.entity.Bank;
import eureca.capstone.project.orchestrator.pay.entity.ChangeType;
import eureca.capstone.project.orchestrator.pay.entity.PayType;
import eureca.capstone.project.orchestrator.pay.repository.BankRepository;
import eureca.capstone.project.orchestrator.pay.repository.ChangeTypeRepository;
import eureca.capstone.project.orchestrator.pay.repository.PayTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitComponent {
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;
    private final StatusRepository statusRepository;
    private final SalesTypeRepository salesTypeRepository;
    private final PayTypeRepository payTypeRepository;
    private final ChangeTypeRepository changeTypeRepository;
    private final AlarmTypeRepository alarmTypeRepository;
    private final BankRepository bankRepository;

    private static final List<Status> HARDCODED_STATUSES = List.of(
            Status.builder().statusId(11L).code("EMAIL_VERIFICATION_PENDING").description("이메일 인증 중").domain("USER").build(),
            Status.builder().statusId(12L).code("ACTIVE").description("활성").domain("USER").build(),
            Status.builder().statusId(13L).code("BANNED").description("차단").domain("USER").build(),
            Status.builder().statusId(14L).code("ON_SALE").description("판매중").domain("FEED").build(),
            Status.builder().statusId(15L).code("EXPIRED").description("기간만료").domain("FEED").build(),
            Status.builder().statusId(16L).code("COMPLETED").description("거래 완료").domain("FEED").build(),
            Status.builder().statusId(17L).code("BLURRED").description("블러 처리").domain("FEED").build(),
            Status.builder().statusId(18L).code("ISSUED").description("발급됨").domain("COUPON").build(),
            Status.builder().statusId(19L).code("USED").description("사용됨").domain("COUPON").build(),
            Status.builder().statusId(20L).code("EXPIRED").description("기간만료").domain("COUPON").build(),
            Status.builder().statusId(21L).code("REQUESTED").description("요청됨").domain("PAYMENT").build(),
            Status.builder().statusId(22L).code("COMPLETED").description("완료").domain("PAYMENT").build(),
            Status.builder().statusId(23L).code("FAILED").description("실패").domain("PAYMENT").build(),
            Status.builder().statusId(24L).code("CANCELED").description("취소").domain("PAYMENT").build(),
            Status.builder().statusId(25L).code("PENDING").description("검수 대기중").domain("REPORT").build(),
            Status.builder().statusId(26L).code("AI_ACCEPTED").description("AI 승인").domain("REPORT").build(),
            Status.builder().statusId(27L).code("AI_REJECTED").description("AI 거절").domain("REPORT").build(),
            Status.builder().statusId(28L).code("ADMIN_ACCEPTED").description("관리자 승인").domain("REPORT").build(),
            Status.builder().statusId(29L).code("ADMIN_REJECTED").description("관리자 거절").domain("REPORT").build(),
            Status.builder().statusId(30L).code("PENDING").description("제재 대기중").domain("RESTRICTION").build(),
            Status.builder().statusId(31L).code("COMPLETED").description("제재 완료").domain("RESTRICTION").build(),
            Status.builder().statusId(32L).code("REJECTED").description("제재 미승인").domain("RESTRICTION").build(),
            Status.builder().statusId(33L).code("READ").description("읽음").domain("ALARM").build(),
            Status.builder().statusId(34L).code("UNREAD").description("안읽음").domain("ALARM").build(),
            Status.builder().statusId(35L).code("PURCHASE").description("구매 알림").domain("NOTIFICATION").build(),
            Status.builder().statusId(36L).code("SALE").description("판매 알림").domain("NOTIFICATION").build(),
            Status.builder().statusId(37L).code("BID").description("입찰 알림").domain("NOTIFICATION").build(),
            Status.builder().statusId(38L).code("COUPON_EXPIRATION").description("쿠폰 만료 알림").domain("NOTIFICATION").build(),
            Status.builder().statusId(39L).code("FEED_EXPIRATION").description("게시글 만료 알림").domain("NOTIFICATION").build(),
            Status.builder().statusId(40L).code("RESTRICT_EXPIRATION").description("제재 만료").domain("RESTRICTION").build(),
            Status.builder().statusId(41L).code("COMPLETED").description("제재 완료").domain("REPORT").build(),
            Status.builder().statusId(42L).code("REJECTED").description("제재 미승인").domain("REPORT").build(),
            Status.builder().statusId(43L).code("DONE").description("결제 승인 완료").domain("TOSS").build(),
            Status.builder().statusId(44L).code("PENDING").description("쿠폰 사용 대기 중").domain("COUPON").build()
    );

    private static final List<SalesType> HARDCODED_SALES_TYPES = List.of(
            SalesType.builder().SalesTypeId(1L).name("일반 판매").build(),
            SalesType.builder().SalesTypeId(2L).name("입찰 판매").build()
    );

    private static final List<PayType> HARDCODED_PAY_TYPES = List.of(
            PayType.builder().payTypeId(1L).name("카드").build(),
            PayType.builder().payTypeId(2L).name("계좌이체").build(),
            PayType.builder().payTypeId(3L).name("토스페이").build(),
            PayType.builder().payTypeId(4L).name("페이코").build(),
            PayType.builder().payTypeId(5L).name("카카오페이").build(),
            PayType.builder().payTypeId(6L).name("네이버페이").build(),
            PayType.builder().payTypeId(7L).name("휴대폰").build(),
            PayType.builder().payTypeId(8L).name("문화상품권").build(),
            PayType.builder().payTypeId(9L).name("가상계좌").build()
    );

    private static final List<ChangeType> HARDCODED_CHANGE_TYPES = List.of(
        ChangeType.builder().changeTypeId(1L).type("충전").content("충전으로 인해 페이 증가").build(),
        ChangeType.builder().changeTypeId(2L).type("환전").content("환전으로 인해 페이 감소").build(),
        ChangeType.builder().changeTypeId(3L).type("구매").content("구매로 인해 페이 감소").build(),
        ChangeType.builder().changeTypeId(4L).type("판매").content("판매로 인해 페이 증가").build()
    );

    private static final List<AlarmType> HARDCODED_ALARM_TYPES = List.of(
        AlarmType.builder().alarmTypeId(1L).type("구매").build(),
        AlarmType.builder().alarmTypeId(2L).type("판매").build(),
        AlarmType.builder().alarmTypeId(3L).type("입찰 성공").build(),
        AlarmType.builder().alarmTypeId(4L).type("쿠폰 만료").build(),
        AlarmType.builder().alarmTypeId(5L).type("게시글 만료").build(),
        AlarmType.builder().alarmTypeId(6L).type("입찰 갱신").build()
    );

    private static final List<Bank> HARDCODED_BANKS = List.of(
        Bank.builder().bankId(1L).bankName("Toss뱅크").build(),
        Bank.builder().bankId(2L).bankName("농협은행").build(),
        Bank.builder().bankId(3L).bankName("하나은행").build(),
        Bank.builder().bankId(4L).bankName("기업은행").build(),
        Bank.builder().bankId(5L).bankName("카카오뱅크").build()
    );

    /**
     * 애플리케이션 시작 시, Role × Authority 의 모든 조합을 확인해서
     * DB에 존재하지 않는 매핑만 RoleAuthority 로 저장한다.
     */
    @PostConstruct
    @Transactional
    public void init() {
        List<Role> roles = roleRepository.findAll();
        List<Authority> auths = authorityRepository.findAll();

        for (Role role : roles) {
            for (Authority auth : auths) {
                // 이미 매핑이 있는지 체크
                boolean exists = roleAuthorityRepository.existsByRoleAndAuthority(role, auth);
                if (!exists) {
                    RoleAuthority ra = RoleAuthority.builder()
                            .role(role)
                            .authority(auth)
                            .build();
                    roleAuthorityRepository.save(ra);
                }
            }
        }
    }

    /**
     * 애플리케이션 시작 시, Status 리스트와 DB를 비교하여
     * DB에 존재하지 않는 Status만 저장합니다.
     */
    @PostConstruct
    @Transactional
    public void initStatuses() {
        // 1. DB의 모든 Status를 ID를 Key로 하는 Map 으로 변환
        Map<Long, Status> dbStatusMap = statusRepository.findAll().stream()
                .collect(Collectors.toMap(Status::getStatusId, Function.identity()));

        // 2. 하드코딩된 리스트를 순회하며 DB에 없거나(new) 내용이 다른(updated) Status를 찾음
        List<Status> statusesToSave = HARDCODED_STATUSES.stream()
                .filter(hardcodedStatus -> {
                    Status dbStatus = dbStatusMap.get(hardcodedStatus.getStatusId());
                    return dbStatus == null || !dbStatus.equals(hardcodedStatus);
                })
                .collect(Collectors.toList());

        if (!statusesToSave.isEmpty()) {
            log.info("{} 개의 status 가 DB 에 존재하지 않거나 변경되었습니다.", statusesToSave.size());
            statusRepository.saveAll(statusesToSave);
        } else {
            log.info("모든 status 가 DB 에 존재합니다.");
        }
    }

    @PostConstruct
    @Transactional
    public void initSalesTypes() {
        Map<Long, SalesType> dbSalesTypeMap = salesTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SalesType::getSalesTypeId, Function.identity()));

        List<SalesType> salesTypesToSave = HARDCODED_SALES_TYPES.stream()
                .filter(hardcodedSalesType -> {
                    SalesType dbSalesType = dbSalesTypeMap.get(hardcodedSalesType.getSalesTypeId());
                    return dbSalesType == null || !dbSalesType.equals(hardcodedSalesType);
                })
                .collect(Collectors.toList());

        if (!salesTypesToSave.isEmpty()) {
            log.info("{} 개의 salesType 이 DB 에 존재하지 않거나 변경되었습니다.", salesTypesToSave.size());
            salesTypeRepository.saveAll(salesTypesToSave);
        } else {
            log.info("모든 salesType 이 DB 에 존재합니다.");
        }
    }

    @PostConstruct
    @Transactional
    public void initPayTypes() {
        Map<Long, PayType> dbPayTypeMap = payTypeRepository.findAll().stream()
                .collect(Collectors.toMap(PayType::getPayTypeId, Function.identity()));

        List<PayType> payTypesToSave = HARDCODED_PAY_TYPES.stream()
                .filter(hardcodedPayType -> {
                    PayType dbPayType = dbPayTypeMap.get(hardcodedPayType.getPayTypeId());
                    return dbPayType == null || !dbPayType.equals(hardcodedPayType);
                })
                .collect(Collectors.toList());

        if (!payTypesToSave.isEmpty()) {
            log.info("{} 개의 payType 이 DB 에 존재하지 않거나 변경되었습니다.", payTypesToSave.size());
            payTypeRepository.saveAll(payTypesToSave);
        } else {
            log.info("모든 payType 이 DB 에 존재합니다.");
        }
    }

    @PostConstruct
    @Transactional
    public void initChangeTypes() {
        Map<Long, ChangeType> dbChangeTypeMap = changeTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ChangeType::getChangeTypeId, Function.identity()));

        List<ChangeType> changeTypesToSave = HARDCODED_CHANGE_TYPES.stream()
                .filter(hardcodedChangeType -> {
                    ChangeType dbChangeType = dbChangeTypeMap.get(hardcodedChangeType.getChangeTypeId());
                    return dbChangeType == null || !dbChangeType.equals(hardcodedChangeType);
                })
                .collect(Collectors.toList());

        if (!changeTypesToSave.isEmpty()) {
            log.info("{} 개의 changeType 이 DB 에 존재하지 않거나 변경되었습니다.", changeTypesToSave.size());
            changeTypeRepository.saveAll(changeTypesToSave);
        } else {
            log.info("모든 changeType 이 DB 에 존재합니다.");
        }
    }

    @PostConstruct
    @Transactional
    public void initAlarmTypes() {
        Map<Long, AlarmType> dbAlarmTypeMap = alarmTypeRepository.findAll().stream()
                .collect(Collectors.toMap(AlarmType::getAlarmTypeId, Function.identity()));

        List<AlarmType> alarmTypesToSave = HARDCODED_ALARM_TYPES.stream()
                .filter(hardcodedAlarmType -> {
                    AlarmType dbAlarmType = dbAlarmTypeMap.get(hardcodedAlarmType.getAlarmTypeId());
                    return dbAlarmType == null || !dbAlarmType.equals(hardcodedAlarmType);
                })
                .collect(Collectors.toList());

        if (!alarmTypesToSave.isEmpty()) {
            log.info("{} 개의 alarmType 이 DB 에 존재하지 않거나 변경되었습니다.", alarmTypesToSave.size());
            alarmTypeRepository.saveAll(alarmTypesToSave);
        } else {
            log.info("모든 alarmType 이 DB 에 존재합니다.");
        }
    }

    @PostConstruct
    @Transactional
    public void initBanks() {
        Map<Long, Bank> dbBankMap = bankRepository.findAll().stream()
                .collect(Collectors.toMap(Bank::getBankId, Function.identity()));

        List<Bank> banksToSave = HARDCODED_BANKS.stream()
                .filter(hardcodedBank -> {
                    Bank dbBank = dbBankMap.get(hardcodedBank.getBankId());
                    return dbBank == null || !dbBank.equals(hardcodedBank);
                })
                .collect(Collectors.toList());

        if (!banksToSave.isEmpty()) {
            log.info("{} 개의 bank 가 DB 에 존재하지 않거나 변경되었습니다.", banksToSave.size());
            bankRepository.saveAll(banksToSave);
        } else {
            log.info("모든 bank 가 DB 에 존재합니다.");
        }
    }
}
