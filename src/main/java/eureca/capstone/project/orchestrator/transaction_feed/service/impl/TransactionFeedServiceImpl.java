package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.repository.StatusRepository;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponse;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.DeductSellableDataRequestDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import eureca.capstone.project.orchestrator.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionFeedServiceImpl implements TransactionFeedService {
    private final TransactionFeedRepository transactionFeedRepository;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final StatusRepository statusRepository;
    private final TelecomCompanyRepository telecomCompanyRepository;
    private final SalesTypeRepository salesTypeRepository;
    private final UserDataService userDataService;


    @Override
    @Transactional
    public CreateFeedResponse createFeed(CreateFeedRequestDto feedRequestDto) {
        // TODO 로그인한 사용자 받아오기
        String email = "test@example.com"; //SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        UserData userData = userDataRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new UserDataNotFoundException());

        // 판매데이터양 < 입력데이터양
        if(userData.getSellableDataMb() < feedRequestDto.getSalesDataAmount()){
            throw new DataOverSellableAmountException();
        }

        // 통신사 조회
        TelecomCompany telecomCompany = telecomCompanyRepository.findById(feedRequestDto.getTelecomCompany())
                .orElseThrow(() -> new TelecomCompanyNotFoundException());

        // 판매타입 조회
        SalesType salesType = salesTypeRepository.findById(feedRequestDto.getSalesType())
                .orElseThrow(() -> new StatusNotFoundException());

        // 상태 조회
        Status status = statusRepository.findByCode("ON_SALE")
                .orElseThrow(() -> new StatusNotFoundException());

        Integer resetDay = userData.getResetDataAt(); // 예: 15
        LocalDate today = LocalDate.now();
        LocalDate nextReset = today.withDayOfMonth(resetDay);

        // 만료일자가 이번달인지 다음달인지 판단
        if (today.getDayOfMonth() >= resetDay) {
            nextReset = nextReset.plusMonths(1);
        }

        LocalDateTime expiresAt = nextReset.minusDays(1).atTime(23,59,59);

        // 게시글 등록
        TransactionFeed transactionFeed = TransactionFeed.builder()
                .user(user)
                .title(feedRequestDto.getTitle())
                .content(feedRequestDto.getContent())
                .telecomCompany(telecomCompany)
                .salesType(salesType)
                .salesPrice(feedRequestDto.getSalesPrice())
                .salesDataAmount(feedRequestDto.getSalesDataAmount())
                .defaultImageNumber(feedRequestDto.getDefaultImageNumber())
                .expiresAt(expiresAt)
                .status(status)
                .isDeleted(false)
                .build();

        transactionFeedRepository.save(transactionFeed);

        // 판매데이터 차감
//        DeductSellableDataRequestDto deductDataRequest = DeductSellableDataRequestDto.builder().
//                userId(user.getUserId())
//                .amount(Integer.valueOf(feedRequestDto.getSalesDataAmount())).build(); // Long -> Integer
//        userDataService.deductSellableData(deductDataRequest);

        return CreateFeedResponse.builder()
                .id(transactionFeed.getTransactionFeedId()).build();
    }
}
