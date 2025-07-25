package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.dto.SalesTypeDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.FeedSort;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.SalesTypeFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.StatusFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.FeedSearchRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.LikedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import co.elastic.clients.elasticsearch._types.Script;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionFeedServiceImpl implements TransactionFeedService {
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final UserDataRepositoryCustom userDataRepositoryCustom;
    private final TelecomCompanyRepository telecomCompanyRepository;
    private final SalesTypeRepository salesTypeRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final LikedRepository likedRepository;
    private final UserDataService userDataService;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;
    private final TransactionFeedSearchRepository transactionFeedSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Map<String, Long> TELECOM_SYNONYM_MAP = new java.util.HashMap<>();
    static {
        List.of("skt", "sk", "에스케이티", "스크", "슼", "스크트", "에스케이")
                .forEach(key -> TELECOM_SYNONYM_MAP.put(key, 1L));
        List.of("kt", "케이티", "킅", "케티", "크트")
                .forEach(key -> TELECOM_SYNONYM_MAP.put(key, 2L));
        List.of("lgu+", "lgu", "u+", "lg", "유플러스", "엘지유플러스", "엘지", "엘쥐", "유플")
                .forEach(key -> TELECOM_SYNONYM_MAP.put(key, 3L));
    }

    @Override
    @Transactional
    public CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto) {
        log.info("[createFeed] 사용자 {} 판매글 작성 시작", email);
        SalesType salesType = salesTypeRepository.findById(feedRequestDto.getSalesTypeId())
                .orElseThrow(SalesTypeNotFoundException::new);

        validateAuctionCreationTime(salesType);
        log.info("[createFeed] 판매글 등록 시간 검증 완료.");

        User user = findUserByEmail(email);
        UserData userData = findAndValidateUserData(user, feedRequestDto.getSalesDataAmount());
        log.info("[createFeed] 사용자 판매 가능 데이터 검증 완료.");

        TransactionFeed transactionFeed = buildNewFeed(user, userData, feedRequestDto, salesType);
        transactionFeedRepository.save(transactionFeed);
        log.info("[createFeed] 판매글 DB 저장 완료. 판매글 ID: {}", transactionFeed.getTransactionFeedId());

        transactionFeedSearchRepository.save(TransactionFeedDocument.fromEntity(transactionFeed));
        log.info("[createFeed] ES Document 저장 완료. Document ID: {}", transactionFeed.getTransactionFeedId());

        userDataService.deductSellableData(user.getUserId(), feedRequestDto.getSalesDataAmount());
        log.info("[createFeed] 판매자 판매 가능 데이터 차감 완료.");

        return CreateFeedResponseDto.builder()
                .id(transactionFeed.getTransactionFeedId())
                .build();

    }

    @Override
    @Transactional
    public UpdateFeedResponseDto updateFeed(String email, UpdateFeedRequestDto updateFeedRequestDto) {
        log.info("[updateFeed] 판매글 수정 시작. 사용자: {}, 판매글 ID: {}", email, updateFeedRequestDto.getTransactionFeedId());

        User user = findUserByEmail(email);
        TransactionFeed transactionFeed = findTransactionFeedById(updateFeedRequestDto.getTransactionFeedId());
        log.info("[updateFeed] 수정하려는 판매글 ID: {}", transactionFeed.getTransactionFeedId());

        if (!transactionFeed.getUser().equals(user)) throw new FeedModifyPermissionException();
        log.info("[updateFeed] 사용자와 판매자가 일치합니다.");

        if (transactionFeed.getSalesType().getName().equals(salesTypeManager.getBidSaleType().getName())) throw new AuctionTypeModifyNotAllowedException();
        log.info("[updateFeed] 입찰 판매가 아닙니다.");

        Status onSaleStatus = statusManager.getStatus("FEED", "ON_SALE");
        if (transactionFeed.isDeleted() || !transactionFeed.getStatus().equals(onSaleStatus)) {
            log.warn("[updateFeed] 삭제되었거나 거래가 완료된 판매글에 대한 수정 요청입니다. ID: {}", transactionFeed.getTransactionFeedId());
            throw new FeedModifyPermissionException();
        }

        handleSaleDataChange(user, transactionFeed.getSalesDataAmount(), updateFeedRequestDto.getSalesDataAmount());

        transactionFeed.update(
                updateFeedRequestDto.getTitle(),
                updateFeedRequestDto.getContent(),
                updateFeedRequestDto.getSalesPrice(),
                updateFeedRequestDto.getSalesDataAmount(),
                updateFeedRequestDto.getDefaultImageNumber()
        );
        log.info("[updateFeed] 판매글 DB 업데이트 완료. 판매글 ID: {}", transactionFeed.getTransactionFeedId());

        TransactionFeedDocument document = transactionFeedSearchRepository.findById(transactionFeed.getTransactionFeedId())
                .orElseThrow(TransactionFeedNotFoundException::new);

        document.updateFields(
                updateFeedRequestDto.getTitle(),
                updateFeedRequestDto.getContent(),
                updateFeedRequestDto.getSalesDataAmount(),
                updateFeedRequestDto.getDefaultImageNumber()
        );
        document.updateNormalPrice(updateFeedRequestDto.getSalesPrice());
        transactionFeedSearchRepository.save(document);
        log.info("[updateFeed] ES Document 업데이트 완료. Document ID: {}", document.getId());

        return UpdateFeedResponseDto.builder()
                .transactionFeedId(transactionFeed.getTransactionFeedId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetFeedDetailResponseDto getFeedDetail(Long transactionFeedId, CustomUserDetailsDto userDetailsDto) {
        log.info("[getFeedDetail] 판매글 상세 조회 시작. ID: {}", transactionFeedId);
        TransactionFeed feed = transactionFeedRepository.findFeedDetailById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);

        boolean isLiked = false;
        if (userDetailsDto != null) {
            User user = findUserByEmail(userDetailsDto.getEmail());
            isLiked = likedRepository.existsByFeedAndUser(feed, user);
        }
        log.info("[getFeedDetail] 찜 여부 조회 완료: {}", isLiked);

        long likedCount = likedRepository.countByTransactionFeed(feed);
        log.info("[getFeedDetail] 찜 횟수 조회 완료: {}", likedCount);

        Long currentHeightPrice = null;

        String auctionType = salesTypeManager.getBidSaleType().getName();
        if (auctionType.equals(feed.getSalesType().getName())) {
            log.info("[getFeedDetail] 입찰 판매글입니다. 현재 최고가 조회");
            String highestPriceKey = String.format("bids:%d:highest_price", feed.getTransactionFeedId());
            String highestPriceStr = stringRedisTemplate.opsForValue().get(highestPriceKey);

            if (highestPriceStr != null) {
                log.info("[getFeedDetail] 압찰 내역이 존재합니다. 최고가: {}", highestPriceStr);
                currentHeightPrice = Long.parseLong(highestPriceStr);
            } else {
                log.info("[getFeedDetail] 압찰 내역이 존재하지 않습니다. 판매글의 최초 가격을 조회합니다.");
                currentHeightPrice = feed.getSalesPrice();
            }
        }

        return GetFeedDetailResponseDto.builder()
                .transactionFeedId(feed.getTransactionFeedId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .salesDataAmount(feed.getSalesDataAmount())
                .salesPrice(feed.getSalesPrice())
                .defaultImageNumber(feed.getDefaultImageNumber())
                .createdAt(feed.getCreatedAt())
                .nickname(feed.getUser().getNickname())
                .liked(isLiked)
                .likedCount(likedCount)
                .telecomCompany(TelecomCompanyDto.fromEntity(feed.getTelecomCompany()))
                .status(StatusDto.fromEntity(feed.getStatus()))
                .salesType(SalesTypeDto.fromEntity(feed.getSalesType()))
                .expiredAt(feed.getExpiresAt())
                .currentHeightPrice(currentHeightPrice)
                .build();
    }

    @Override
    @Transactional
    public void deleteFeed(String email, Long transactionFeedId) {
        log.info("[deleteFeed] 사용자 {}, 판매글 {} 삭제 시작", email, transactionFeedId);

        TransactionFeed transactionFeed = findTransactionFeedById(transactionFeedId);
        log.info("[deleteFeed] 삭제하려는 판매글 조회 완료.");

        if (!transactionFeed.getUser().getEmail().equals(email)) throw new FeedModifyPermissionException();
        log.info("[deleteFeed] 사용자와 판매자가 일치합니다.");

        if (transactionFeed.getSalesType().getName().equals(salesTypeManager.getBidSaleType().getName())) throw new AuctionTypeModifyNotAllowedException();
        log.info("[deleteFeed] 입찰 판매가 아닙니다.");

        if (transactionFeed.isDeleted()) {
            log.warn("[deleteFeed] 이미 삭제된 판매글(ID: {})에 대한 요청입니다.", transactionFeedId);
            return;
        }

        transactionFeed.delete();
        log.info("[deleteFeed] 판매글 논리적 삭제 완료");

        transactionFeedSearchRepository.save(TransactionFeedDocument.fromEntity(transactionFeed));
        log.info("[deleteFeed] ES Document 삭제 상태 업데이트 완료. Document ID: {}", transactionFeed.getTransactionFeedId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetFeedSummaryResponseDto> searchFeeds(FeedSearchRequestDto requestDto, Pageable pageable, CustomUserDetailsDto userDetailsDto) {
        log.info("[searchFeeds] 판매글 검색 요청. Keyword: '{}', Filters: {}, Pageable: {}",
                requestDto.getKeyword(), requestDto, pageable);

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        boolQueryBuilder.filter(f -> f.term(t -> t.field("isDeleted").value(false)));

        Status blurredStatus = statusManager.getStatus("FEED", "BLURRED");
        boolQueryBuilder.mustNot(mn -> mn.term(t -> t.field("status").value(blurredStatus.getCode())));
        log.info("[searchFeeds] 'BLURRED' 상태 코드 '{}'를 검색에서 제외", blurredStatus.getCode());

        String keyword = requestDto.getKeyword();

        if (keyword != null && !keyword.isBlank()) {
            log.info("[searchFeeds] 키워드 '{}' 분석을 시작합니다.", keyword);
            List<String> textKeywords = new ArrayList<>();

            // 키워드를 공백 기준으로 단어별로 분리
            String[] words = keyword.trim().split("\\s+");

            for (String word : words) {
                String lowerWord = word.toLowerCase();

                // 1. 통신사 동의어인지 확인
                if (TELECOM_SYNONYM_MAP.containsKey(lowerWord)) {
                    Long telecomId = TELECOM_SYNONYM_MAP.get(lowerWord);
                    log.info("[searchFeeds] >> 단어 '{}'에서 통신사 ID {}를 감지. 필터에 추가합니다.", word, telecomId);
                    boolQueryBuilder.filter(f -> f.term(t -> t.field("telecomCompanyId").value(telecomId)));
                    continue;
                }

                // 2. 데이터 크기인지 확인
                Long parsedAmount = parseDataAmount(word);
                if (parsedAmount != null) {
                    log.info("[searchFeeds] >> 단어 '{}'에서 데이터 크기 {}MB를 감지. 필터에 추가합니다.", word, parsedAmount);
                    boolQueryBuilder.filter(f -> f.term(t -> t.field("salesDataAmount").value(parsedAmount)));
                    continue;
                }

                textKeywords.add(word);
            }

            // 3. 남은 텍스트 키워드가 있으면 multi-match 검색 실행
            if (!textKeywords.isEmpty()) {
                String searchText = String.join(" ", textKeywords);
                log.info("[searchFeeds] >> 나머지 단어 '{}'에 대해 텍스트 검색을 수행합니다.", searchText);
                boolQueryBuilder.filter(f -> f.multiMatch(mm -> mm
                        .query(searchText)
                        .fields("title", "content", "nickname", "telecomCompanyName")
                ));
            }
        }

        applyDynamicFilters(boolQueryBuilder, requestDto);
        log.info("[searchFeeds] 동적 필터 적용 완료.");


        FeedSort feedSort = requestDto.getSortBy();
        String sortProperty = feedSort.getProperty();
        Sort.Direction direction = feedSort.getDirection();

        if (feedSort == FeedSort.PRICE_HIGH || feedSort == FeedSort.PRICE_LOW) {
            sortProperty = "sortPrice";
            log.info("[searchFeeds] 가격 정렬 요청 -> '{}' 필드로 정렬합니다.", sortProperty);
        }

        Sort customSort = Sort.by(direction, sortProperty);
        Pageable customPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), customSort);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(customPageable)
                .build();

        SearchHits<TransactionFeedDocument> searchHits = elasticsearchOperations.search(nativeQuery, TransactionFeedDocument.class);
        log.info("[searchFeeds] Elasticsearch 쿼리 실행 완료. 총 {}개 검색.", searchHits.getTotalHits());

        Set<Long> likedFeedIds = getLikedFeedIds(userDetailsDto, searchHits);
        log.info("[searchFeeds] 찜 목록 조회 완료.");

        Page<GetFeedSummaryResponseDto> responseDtoPage = toDtoPage(searchHits, pageable, likedFeedIds);
        log.info("[searchFeeds] 검색 결과 DTO 변환 완료. 변환된 결과 수: {}", responseDtoPage.getNumberOfElements());

        return responseDtoPage;
    }

    @Override
    public Page<GetFeedSummaryResponseDto> getMyFeeds(CustomUserDetailsDto userDetailsDto, SalesTypeFilter salesTypeFilter, StatusFilter statusFilter, Pageable pageable) {
        String email = userDetailsDto.getEmail();
        User user = findUserByEmail(email);
        log.info("[getMyFeeds] 사용자 {}의 판매글 조회 시작. 필터: {}, 상태: {}, 페이지: {}", user.getUserId(), salesTypeFilter, statusFilter, pageable);

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        boolQueryBuilder.filter(f -> f.term(t -> t.field("sellerId").value(user.getUserId())));
        boolQueryBuilder.filter(f -> f.term(t -> t.field("isDeleted").value(false)));

        if (salesTypeFilter == SalesTypeFilter.NORMAL) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("salesTypeId").value(salesTypeManager.getNormalSaleType().getSalesTypeId())));
        } else if (salesTypeFilter == SalesTypeFilter.BID) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("salesTypeId").value(salesTypeManager.getBidSaleType().getSalesTypeId())));
        }

        if (statusFilter == StatusFilter.ON_SALE) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("status").value(statusManager.getStatus("FEED", "ON_SALE").getCode())));
        } else if (statusFilter == StatusFilter.COMPLETED) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("status").value(statusManager.getStatus("FEED", "COMPLETED").getCode())));
        } else if (statusFilter == StatusFilter.EXPIRED) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("status").value(statusManager.getStatus("FEED", "EXPIRED").getCode())));
        }

        String sortProperty = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("createdAt");
        Sort.Direction direction = pageable.getSort().stream().findFirst().map(Sort.Order::getDirection).orElse(Sort.Direction.DESC);

        if ("salesPrice".equals(sortProperty)) {
            sortProperty = "sortPrice";
            log.info("[getMyFeeds] 가격 정렬 요청 -> '{}' 필드로 정렬합니다.", sortProperty);
        }

        Sort customSort = Sort.by(direction, sortProperty);
        Pageable customPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), customSort);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(customPageable)
                .build();

        SearchHits<TransactionFeedDocument> searchHits = elasticsearchOperations.search(nativeQuery, TransactionFeedDocument.class);
        log.info("[getMyFeeds] Elasticsearch 쿼리 실행 완료. 총 {}개 검색.", searchHits.getTotalHits());

        Set<Long> likedFeedIds = getLikedFeedIds(userDetailsDto, searchHits);

        return toDtoPage(searchHits, pageable, likedFeedIds);
    }

    @Override
    @Transactional
    public long reindexAllFeeds() {
        log.info("[reindexAllFeeds] 인덱스 재생성 및 재색인 시작");

        if (elasticsearchOperations.indexOps(TransactionFeedDocument.class).exists()) {
            elasticsearchOperations.indexOps(TransactionFeedDocument.class).delete();
        }

        elasticsearchOperations.indexOps(TransactionFeedDocument.class).create();
        elasticsearchOperations.indexOps(TransactionFeedDocument.class).putMapping();

        List<TransactionFeed> allFeeds = transactionFeedRepository.findAll();
        if (allFeeds.isEmpty()) {
            log.warn("[reindexAllFeeds] 재색인할 데이터가 없습니다.");
            return 0;
        }

        Map<Long, Long> highestPriceMap = getHighestPricesFromRedisForFeeds(allFeeds);
        log.info("[reindexAllFeeds] Redis에서 {}개의 입찰 판매글 현재가 정보를 조회.", highestPriceMap.size());

        Long bidSalesTypeId = salesTypeManager.getBidSaleType().getSalesTypeId();

        List<TransactionFeedDocument> documents = allFeeds.stream()
                .map(feed -> {
                    TransactionFeedDocument doc = TransactionFeedDocument.fromEntity(feed);

                    boolean isAuction = feed.getSalesType().getSalesTypeId().equals(bidSalesTypeId);
                    if (isAuction && highestPriceMap.containsKey(doc.getId())) {
                        doc.updateHighestPrice(highestPriceMap.get(doc.getId()));
                    }
                    return doc;
                })
                .toList();

        transactionFeedSearchRepository.saveAll(documents);
        log.info("[reindexAllFeeds] {}개의 문서를 재색인 완료.", documents.size());
        return documents.size();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepository.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private void validateAuctionCreationTime(SalesType salesType) {
        if (salesTypeManager.getBidSaleType().getName().equals(salesType.getName())) {
            LocalTime now = LocalTime.now();
            LocalTime restrictionStartTime = LocalTime.of(23, 30);

            if (!now.isBefore(restrictionStartTime)) {
                log.info("[validateAuctionCreationTime] 입찰 판매글 등록 제한 시간입니다. 현재시간: {}", now);
                throw new AuctionCreationNotAllowedException();
            }
        }
    }

    private UserData findAndValidateUserData(User user, long salesDataAmount) {
        UserData userData = userDataRepositoryCustom.findByUserIdWithLock(user.getUserId())
                .orElseThrow(UserDataNotFoundException::new);

        if (userData.getSellableDataMb() < salesDataAmount) {
            throw new DataOverSellableAmountException();
        }
        return userData;
    }

    private TransactionFeed buildNewFeed(User user, UserData userData, CreateFeedRequestDto dto, SalesType salesType) {
        TelecomCompany telecomCompany = telecomCompanyRepository.findById(dto.getTelecomCompanyId())
                .orElseThrow(TelecomCompanyNotFoundException::new);

        if (user.getTelecomCompany() != telecomCompany) {
            throw new InvalidTelecomCompanyException();
        }

        Status status = statusManager.getStatus("FEED", "ON_SALE");

        LocalDateTime expiresAt;

        if (salesTypeManager.getBidSaleType().getName().equals(salesType.getName())) {
            log.info("[buildNewFeed] 입찰 판매글이므로 만료일은 오늘 자정입니다.");
            expiresAt = LocalDate.now().atTime(LocalTime.MAX);

        } else {
            log.info("[buildNewFeed] 일반 판매글이므로 만료일은 사용자 데이터 리셋일 기준입니다.");
            expiresAt = calculateExpirationDate(userData.getResetDataAt());
        }

        return TransactionFeed.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .telecomCompany(telecomCompany)
                .salesType(salesType)
                .salesPrice(dto.getSalesPrice())
                .salesDataAmount(dto.getSalesDataAmount())
                .defaultImageNumber(dto.getDefaultImageNumber())
                .expiresAt(expiresAt)
                .status(status)
                .isDeleted(false)
                .build();
    }

    private LocalDateTime calculateExpirationDate(int resetDay) {
        LocalDate today = LocalDate.now();
        LocalDate nextReset = today.withDayOfMonth(resetDay);

        if (today.getDayOfMonth() >= resetDay) {
            nextReset = nextReset.plusMonths(1);
        }
        return nextReset.minusDays(1).atTime(23, 59, 59);
    }

    /**
     * 판매글 수정시 판매 데이터 양 변경에 따른 사용자 데이터 증가, 감소를 처리합니다.
     *
     * @param user               사용자 엔티티
     * @param originalSaleDataMb 기존 판매 데이터 양
     * @param newSaleDataMb      새로운 판매 데이터 양
     */
    private void handleSaleDataChange(User user, long originalSaleDataMb, long newSaleDataMb) {
        long saleDataChangeAmount = newSaleDataMb - originalSaleDataMb;

        if (saleDataChangeAmount > 0) {
            log.info("[handleSaleDataChange] 판매 데이터 증가. 추가량: {}", saleDataChangeAmount);
            findAndValidateUserData(user, saleDataChangeAmount);
            userDataService.deductSellableData(user.getUserId(), saleDataChangeAmount);
            log.info("[handleSaleDataChange] 사용자 판매 가능 데이터 추가 차감 완료.");

        } else if (saleDataChangeAmount < 0) {
            long refundDataAmount = Math.abs(saleDataChangeAmount);
            log.info("[handleSaleDataChange] 판매 데이터 감소. 환불량: {}", refundDataAmount);
            userDataService.addSellableData(user.getUserId(), refundDataAmount);
            log.info("[handleSaleDataChange] 사용자 판매 가능 데이터 환불 완료");

        } else {
            log.info("[handleSaleDataChange] 판매 데이터 양 변경 없음.");
        }
    }

    private Long parseDataAmount(String word) {
        if (word == null) {
            return null;
        }
        String lowerWord = word.toLowerCase();

        try {
            String numberPart = lowerWord.replaceAll("[^0-9.]", "");
            if (numberPart.isEmpty()) {
                return null;
            }
            double numericValue = Double.parseDouble(numberPart);

            if (lowerWord.contains("tb") || lowerWord.contains("테라")) {
                return (long) (numericValue * 1000 * 1000);
            }
            if (lowerWord.contains("gb") || lowerWord.contains("기가")) {
                return (long) (numericValue * 1000);
            }
            if (lowerWord.contains("mb") || lowerWord.contains("메가")) {
                return (long) numericValue;
            }

        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }

    private void applyDynamicFilters(BoolQuery.Builder boolBuilder, FeedSearchRequestDto request) {
        addTermsFilter(boolBuilder, "telecomCompanyId", request.getTelecomCompanyIds());
        addTermsFilter(boolBuilder, "salesTypeId", request.getSalesTypeIds());
        addTermsFilter(boolBuilder, "status", request.getStatuses());

        addRangeFilter(boolBuilder, "salesPrice", request.getMinPrice(), request.getMaxPrice());
        addRangeFilter(boolBuilder, "salesDataAmount", request.getMinDataAmount(), request.getMaxDataAmount());

        addExclusionFilter(boolBuilder, request.getExcludeFeedIds());
    }

    private <T> void addTermsFilter(BoolQuery.Builder boolBuilder, String field, List<T> values) {
        if (!CollectionUtils.isEmpty(values)) {
            List<FieldValue> fieldValues = values.stream()
                    .map(FieldValue::of)
                    .toList();

            boolBuilder.filter(f -> f.terms(t -> t
                    .field(field)
                    .terms(new TermsQueryField.Builder().value(fieldValues).build())
            ));
        }
    }

    private void addRangeFilter(BoolQuery.Builder boolBuilder, String field, Number min, Number max) {
        if (min == null && max == null) {
            return;
        }

        boolBuilder.filter(f -> f.range(rangeQuery -> rangeQuery
                .number(numRangeQuery -> {
                    numRangeQuery.field(field);

                    if (min != null) numRangeQuery.gte(min.doubleValue());
                    if (max != null) numRangeQuery.lte(max.doubleValue());

                    return numRangeQuery;
                })
        ));
    }

    private void addExclusionFilter(BoolQuery.Builder boolBuilder, List<Long> excludeIds) {
        if (!CollectionUtils.isEmpty(excludeIds)) {
            log.info("[searchFeeds] {}개의 판매글 ID를 결과에서 제외.", excludeIds.size());
            List<FieldValue> fieldValues = excludeIds.stream()
                    .map(FieldValue::of)
                    .toList();

            boolBuilder.mustNot(mn -> mn.terms(t -> t
                    .field("id")
                    .terms(new TermsQueryField.Builder().value(fieldValues).build())
            ));
        }
    }

    private Set<Long> getLikedFeedIds(CustomUserDetailsDto userDetailsDto, SearchHits<TransactionFeedDocument> searchHits) {
        log.info("[getLikedFeedIds] 사용자 {}의 찜 목록 조회 시작", userDetailsDto != null ? userDetailsDto.getUserId() : "비회원");
        if (userDetailsDto == null) {
            return Collections.emptySet();
        }
        List<Long> feedIds = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();

        if (feedIds.isEmpty()) {
            return Collections.emptySet();
        }
        User user = findUserByEmail(userDetailsDto.getEmail());
        log.info("[getLikedFeedIds] 사용자({})의 찜 목록 확인", userDetailsDto.getUserId());
        return new HashSet<>(likedRepository.findLikedFeedIdsByUserAndFeedIds(user, feedIds));
    }

    private Map<Long, Long> getHighestPricesFromRedisForFeeds(List<TransactionFeed> feeds) {
        log.info("[getHighestPrices] Redis 에서 판매글 최고가 조회 시작. 판매글 수: {}", feeds.size());
        Long bidSalesTypeId = salesTypeManager.getBidSaleType().getSalesTypeId();

        List<Long> auctionFeedIds = feeds.stream()
                .filter(feed -> bidSalesTypeId.equals(feed.getSalesType().getSalesTypeId()))
                .map(TransactionFeed::getTransactionFeedId)
                .collect(Collectors.toList());

        return fetchHighestPricesFromRedis(auctionFeedIds);
    }

    private Map<Long, Long> fetchHighestPricesFromRedis(List<Long> auctionFeedIds) {
        if (CollectionUtils.isEmpty(auctionFeedIds)) {
            return Collections.emptyMap();
        }

        List<String> redisKeys = auctionFeedIds.stream()
                .map(id -> String.format("bids:%d:highest_price", id))
                .collect(Collectors.toList());

        log.info("[fetchHighestPrices] {}개의 입찰 판매글 최고가를 Redis 에서 조회", redisKeys.size());
        List<String> highestPrices = stringRedisTemplate.opsForValue().multiGet(redisKeys);

        return IntStream.range(0, auctionFeedIds.size())
                .filter(i -> highestPrices != null && highestPrices.get(i) != null)
                .boxed()
                .collect(Collectors.toMap(
                        auctionFeedIds::get,
                        i -> Long.parseLong(highestPrices.get(i))
                ));
    }

    private Page<GetFeedSummaryResponseDto> toDtoPage(SearchHits<TransactionFeedDocument> searchHits, Pageable pageable, Set<Long> likedFeedIds) {
        log.info("[toDtoPage] searchHits -> dto 변환 시작.");

        SalesType normalSalesType = salesTypeManager.getNormalSaleType();
        SalesType auctionSalesType = salesTypeManager.getBidSaleType();

        List<GetFeedSummaryResponseDto> dtoList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> {
                    String salesType = doc.getSalesTypeId().equals(normalSalesType.getSalesTypeId()) ? normalSalesType.getName() : auctionSalesType.getName();

                    return GetFeedSummaryResponseDto.builder()
                            .transactionFeedId(doc.getId())
                            .title(doc.getTitle())
                            .nickname(doc.getNickname())
                            .salesPrice(doc.getSalesPrice())
                            .currentHeightPrice(doc.getCurrentHighestPrice())
                            .salesDataAmount(doc.getSalesDataAmount())
                            .telecomCompany(doc.getTelecomCompanyName())
                            .createdAt(doc.getCreatedAt())
                            .liked(likedFeedIds.contains(doc.getId()))
                            .status(doc.getStatus())
                            .salesType(salesType)
                            .defaultImageNumber(doc.getDefaultImageNumber())
                            .build();
                })
                .collect(Collectors.toList());

        log.info("[toDtoPage] searchHits -> dto 변환 완료.");
        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }
}
