package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.StatusNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.TelecomCompanyNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.repository.StatusRepository;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponse;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
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
        String email = "sbi1@naver.com"; //SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException());

        UserData userData = userDataRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("UserData not found"));

        // 판매데이터양 > 입력데이터양 : 예외 (커스텀예외로 수정)
        if(userData.getSellableDataMb() < feedRequestDto.getSalesDataAmount()){
            throw new IllegalArgumentException("판매데이터 양보다 입력 데이터 양이 더 많습니다.");
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
                .expiresAt(LocalDateTime.now())// TODO 수정!!!
                .status(status)
                .isDeleted(false)
                .build();

        transactionFeedRepository.save(transactionFeed);

        // 판매데이터 차감
//        userDataService.deductSellableData(user.getUserId(), feedRequestDto.getSalesDataAmount());

        return CreateFeedResponse.builder()
                .id(transactionFeed.getTransactionFeedId()).build();
    }
}
