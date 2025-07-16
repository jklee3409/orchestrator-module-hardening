package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionFeedServiceImpl implements TransactionFeedService {
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;
    private final TelecomCompanyRepository telecomCompanyRepository;
    private final SalesTypeRepository salesTypeRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final UserDataService userDataService;
    private final UserDataRepositoryCustom userDataRepositoryCustom;
    private final StatusManager statusManager;

    @Override
    @Transactional
    public CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto) {
        User user = findUserByEmail(email);
        UserData userData = findAndValidateUserData(user, feedRequestDto.getSalesDataAmount());

        TransactionFeed transactionFeed = buildNewFeed(user, userData, feedRequestDto);

        transactionFeedRepository.save(transactionFeed);
        userDataService.deductSellableData(user.getUserId(), feedRequestDto.getSalesDataAmount());

        return CreateFeedResponseDto.builder()
                .id(transactionFeed.getTransactionFeedId())
                .build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private UserData findAndValidateUserData(User user, long salesDataAmount) {
        UserData userData = userDataRepositoryCustom.findByUserIdWithLock(user.getUserId())
                .orElseThrow(UserDataNotFoundException::new);

        if (userData.getSellableDataMb() < salesDataAmount) {
            throw new DataOverSellableAmountException();
        }
        return userData;
    }

    private TransactionFeed buildNewFeed(User user, UserData userData, CreateFeedRequestDto dto) {
        TelecomCompany telecomCompany = telecomCompanyRepository.findById(dto.getTelecomCompanyId())
                .orElseThrow(TelecomCompanyNotFoundException::new);

        if (user.getTelecomCompany() != telecomCompany) throw new InvalidTelecomCompanyException();

        SalesType salesType = salesTypeRepository.findById(dto.getSalesTypeId())
                .orElseThrow(StatusNotFoundException::new);

        Status status = statusManager.getStatus("FEED", "ON_SALE");

        LocalDateTime expiresAt = calculateExpirationDate(userData.getResetDataAt());

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
}
