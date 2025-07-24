package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.SalesTypeFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Liked;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.LikedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikedServiceImplTest {

    @InjectMocks
    private LikedServiceImpl likedService;

    @Mock
    private LikedRepository likedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionFeedRepository transactionFeedRepository;

    @Mock
    private SalesTypeManager salesTypeManager;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private User user;
    private TransactionFeed transactionFeed;
    private SalesType bidSalesType;
    private TelecomCompany telecomCompany;
    private Status onSaleStatus;

    @BeforeEach
    void setUp() {
        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("테스트통신사")
                .build();

        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .telecomCompany(telecomCompany)
                .build();

        SalesType normalSaleType = SalesType.builder()
                .SalesTypeId(1L)
                .name("일반판매")
                .build();

        bidSalesType = SalesType.builder()
                .SalesTypeId(2L)
                .name("입찰판매")
                .build();

        onSaleStatus = Status.builder()
                .statusId(1L)
                .domain("FEED")
                .code("ON_SALE")
                .build();

        transactionFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(user)
                .title("테스트 판매글")
                .salesType(normalSaleType)
                .telecomCompany(telecomCompany)
                .status(onSaleStatus)
                .build();
    }

    @Nested
    @DisplayName("찜 목록 조회")
    class GetWishList {

        @Test
        @DisplayName("찜한 목록이 없을 경우 빈 페이지 반환")
        void getWishList_Empty() {
            // given
            SalesTypeFilter filter = SalesTypeFilter.ALL;
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(likedRepository.findFeedIdsByUser(user)).thenReturn(Collections.emptyList());

            // when
            Page<GetFeedSummaryResponseDto> result = likedService.getWishList(user.getEmail(), filter, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isEmpty()).isTrue();
            verify(transactionFeedRepository, never()).findWishedFeeds(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("찜 추가/삭제 관리")
    class WishFeedAddRemoveOperations {

        @Test
        @DisplayName("찜 목록 추가 성공")
        void addWishFeed_Success() {
            // given
            String email = user.getEmail();
            AddWishFeedRequestDto requestDto = new AddWishFeedRequestDto();
            requestDto.setTransactionFeedId(1L);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(transactionFeedRepository.findById(requestDto.getTransactionFeedId())).thenReturn(Optional.of(transactionFeed));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(false);

            // when
            likedService.addWishFeed(email, requestDto);

            // then
            verify(likedRepository).save(any(Liked.class));
        }

        @Test
        @DisplayName("이미 찜한 피드 추가 시 예외 발생")
        void addWishFeed_AlreadyExists_ThrowsException() {
            // given
            String email = user.getEmail();
            AddWishFeedRequestDto requestDto = new AddWishFeedRequestDto();
            requestDto.setTransactionFeedId(1L);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(transactionFeedRepository.findById(requestDto.getTransactionFeedId())).thenReturn(Optional.of(transactionFeed));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(true);

            // when & then
            assertThrows(InternalServerException.class, () -> likedService.addWishFeed(email, requestDto));
            verify(likedRepository, never()).save(any(Liked.class));
        }

        @Test
        @DisplayName("찜 목록에서 여러 개 삭제 성공")
        void removeWishFeeds_Success() {
            // given
            String email = user.getEmail();
            RemoveWishFeedsRequestDto requestDto = new RemoveWishFeedsRequestDto();
            requestDto.setTransactionFeedIds(List.of(1L, 2L, 3L));

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            // when
            likedService.removeWishFeed(email, requestDto);

            // then
            verify(likedRepository).removeByUserAndFeedIds(requestDto.getTransactionFeedIds(), user);
        }

        @Test
        @DisplayName("빈 리스트로 찜 삭제 요청 시 아무 작업도 하지 않음")
        void removeWishFeeds_WithEmptyList_DoesNothing() {
            // given
            String email = user.getEmail();
            RemoveWishFeedsRequestDto requestDto = new RemoveWishFeedsRequestDto();
            requestDto.setTransactionFeedIds(Collections.emptyList());

            // when
            likedService.removeWishFeed(email, requestDto);

            // then
            verify(userRepository, never()).findByEmail(anyString());
            verify(likedRepository, never()).removeByUserAndFeedIds(any(), any());
        }
    }
}