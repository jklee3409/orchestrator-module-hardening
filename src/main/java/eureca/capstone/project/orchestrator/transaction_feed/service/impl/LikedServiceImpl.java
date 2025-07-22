package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Liked;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.LikedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.LikedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikedServiceImpl implements LikedService {
    private final LikedRepository likedRepository;
    private final UserRepository userRepository;
    private final TransactionFeedRepository transactionFeedRepository;

    @Override
    @Transactional
    public void addWishFeed(String email, AddWishFeedRequestDto requestDto) {
        User user = findUserByEmail(email);
        TransactionFeed transactionFeed = findTransactionFeedById(requestDto.getTransactionFeedId());
        log.info("[addWishFeed] 사용자 및 판매글 조회 완료.");

        if (transactionFeed.isDeleted()) throw new TransactionFeedNotFoundException();
        if (likedRepository.existsByFeedAndUser(transactionFeed, user)) throw new InternalServerException(ErrorCode.ALREADY_EXISTS_LIKED_LIST);
        log.info("[addWishFeed] 찜 목록에 존재 X.");

        Liked liked = Liked.builder()
                .user(user)
                .transactionFeed(transactionFeed)
                .build();
        likedRepository.save(liked);
        log.info("[addWishFeed] 찜 목록에 추가 완료. 사용자: {}, 판매글: {}", user.getUserId(), transactionFeed.getTransactionFeedId());
    }

    @Override
    @Transactional
    public void removeWishFeed(String email, RemoveWishFeedsRequestDto requestDto) {
        List<Long> transactionFeedIds = requestDto.getTransactionFeedIds();

        if (CollectionUtils.isEmpty(transactionFeedIds)) {
            log.info("[removeWishFeed] 삭제할 ID 리스트가 비어있어 작업을 종료합니다.");
            return;
        }

        User user = findUserByEmail(email);
        log.info("[removeWishFeed] 사용자 {}의 찜 목록 삭제 시작. 대상 판매글 수: {}", user.getUserId(), transactionFeedIds.size());

        likedRepository.removeByUserAndFeedIds(transactionFeedIds, user);
        log.info("[removeWishFeed] 찜 목록에서 삭제 완료. 사용자: {}, 대상 판매글: {}", user.getUserId(), transactionFeedIds);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepository.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }
}
