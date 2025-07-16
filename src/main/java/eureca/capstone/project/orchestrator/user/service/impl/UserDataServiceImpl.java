package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.UpdateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.AddBuyerDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateUserDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.DeductSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;

    /**
     * 사용자 데이터 레코드를 생성합니다.
     * 사용자 ID, 요금제 ID, 월간 데이터 용량, 데이터 리셋 일자 등의 정보를 포함한 사용자 데이터를 생성합니다.
     *
     * @param createUserDataRequestDto 사용자 데이터 생성에 필요한 정보(사용자 ID, 요금제 ID, 월간 데이터 용량, 리셋 일자 등)
     * @throws InternalServerException 사용자 데이터 생성 중 오류 발생 시
     */
    @Override
    @Transactional
    public void createUserData(CreateUserDataRequestDto createUserDataRequestDto) {
        log.info("[createUserData] 사용자 데이터 등록 요청");
        try {
            log.info("[createUserData] 사용자 데이터 레코드 등록 시작: userId={}", createUserDataRequestDto.getUserId());
            UserData createUserData = userDataRepository.save(CreateUserDataRequestDto.toEntity(createUserDataRequestDto));
            log.info("[createUserData] 사용자 데이터 레코드 등록 완료: userId={}", createUserDataRequestDto.getUserId());

            CreateUserDataResponseDto.builder()
                    .userDataId(createUserData.getUserDataId())
                    .build();

        } catch (Exception e) {
            log.error("[createUserData] 사용자 데이터 등록 중 오류 발생: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorCode.USER_DATA_CREATE_FAIL);
        }
    }

    /**
     * 사용자의 데이터 현황을 조회합니다.
     * 사용자 ID를 기반으로 사용자의 보유 데이터, 판매 가능 데이터, 구매 데이터 현황을 조회합니다.
     *
     * @param email 조회할 사용자의 email
     * @return 사용자의 데이터 현황
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    public GetUserDataStatusResponseDto getUserDataStatus(String email) {
        log.info("[getUserDataStatus] 사용자 {} 의 데이터 현황 조회", email);

        UserData userData = findUserDataByEmail(email);

        return GetUserDataStatusResponseDto.fromEntity(userData);
    }

    /**
     * 사용자의 보유 데이터를 판매 가능한 데이터로 전환합니다.
     * 사용자 ID를 기반으로 사용자를 찾아 지정된 양의 데이터를 보유 데이터에서 차감하고 판매 가능한 데이터로 전환합니다.
     *
     * @param requestDto 사용자 ID와 판매 가능한 데이터로 전환할 양
     * @return 업데이트된 사용자의 총 데이터와 판매 가능한 데이터
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws InternalServerException 보유 데이터가 부족하거나 전환 중 오류 발생 시
     */
    @Override
    @Transactional
    public CreateSellableDataResponseDto createSellableData(String email, UpdateUserDataRequestDto requestDto) {
        log.info("[createSellableData] 사용자 {} 보유 데이터에서 판매 가능 데이터로 전환", email);
        try {
            UserData userData = findUserDataByEmail(email);

            if (userData.getTotalDataMb() < requestDto.getAmount()) {
                throw new InternalServerException(ErrorCode.USER_TOTAL_DATA_LACK);
            }

            userData.createSellableData(requestDto.getAmount());
            log.info("[createSellableData] 사용자 {} 보유 데이터에서 판매 가능한 데이터로 전환 완료. 최종 보유 데이터: {}, 최종 판매 가능한 데이터: {}",
                    userData.getUserId(), userData.getTotalDataMb(), userData.getSellableDataMb());

            return CreateSellableDataResponseDto.builder()
                    .userId(userData.getUserId())
                    .totalDataMb(userData.getTotalDataMb())
                    .sellableDataMb(userData.getSellableDataMb())
                    .build();

        } catch (Exception e) {
            log.error("[createSellableData] 보유 데이터에서 판매 가능한 데이터로 전환 도중 오류 발생");
            throw new InternalServerException(ErrorCode.SELLABLE_DATA_CREATE_FAIL);
        }
    }

    /**
     * 사용자의 판매 가능한 데이터를 차감합니다.
     * 사용자 ID를 기반으로 사용자를 찾아 지정된 양의 데이터를 판매 가능한 데이터에서 차감합니다.
     * 데이터 판매 시 사용됩니다.
     *
     * @param amount 차감할 데이터 양
     * @return 업데이트된 사용자의 판매 가능한 데이터
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws InternalServerException 판매 가능한 데이터가 부족하거나 차감 중 오류 발생 시
     */
    @Override
    @Transactional
    public DeductSellableDataResponseDto deductSellableData(Long userId, Long amount) {
        log.info("[deductSellableData] 사용자 {} 판매 가능데이터 차감", userId);
        try {
            UserData userData = findUserById(userId);

            if (userData.getSellableDataMb() < amount) {
                throw new InternalServerException(ErrorCode.USER_SELLABLE_DATA_LACK);
            }

            userData.deductSellableData(amount);
            log.info("[deductSellableData] 사용자 {} 판매 가능 데이터 차감 완료. 최종 판매 가능 데이터: {}",
                    userData.getUserId(), userData.getSellableDataMb());

            return DeductSellableDataResponseDto.builder()
                    .userId(userData.getUserId())
                    .sellableDataMb(userData.getSellableDataMb())
                    .build();

        } catch (Exception e) {
            log.error("[deductSellableData] 판매 가능 데이터 차감 도중 오류 발생");
            throw new InternalServerException(ErrorCode.SELLABLE_DATA_DEDUCT_FAIL);
        }
    }

    /**
     * 사용자의 구매 데이터를 충전합니다.
     * 사용자 ID를 기반으로 사용자를 찾아 지정된 양의 데이터를 구매 데이터에 추가합니다.
     * 데이터 충전권을 사용해서 데이터 충전 시 사용됩니다.
     *
     * @param amount 충전할 데이터 양
     * @return 업데이트된 사용자의 구매 데이터
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws InternalServerException 데이터 충전 중 오류 발생 시
     */
    @Override
    @Transactional
    public AddBuyerDataResponseDto chargeBuyerData(Long userId, Long amount) {
        log.info("[chargeBuyerData] 사용자 {} 구매 데이터 충전", userId);
        try {
            UserData userData = findUserById(userId);

            userData.addBuyerData(amount);
            log.info("[chargeBuyerData] 사용자 {} 구매 데이터 충전 완료. 최종 구매 데이터: {}",
                    userData.getUserId(), userData.getBuyerDataMb());

            return AddBuyerDataResponseDto.builder()
                    .userId(userData.getUserId())
                    .buyerDataMb(userData.getBuyerDataMb())
                    .build();

        } catch (Exception e) {
            log.error("[chargeBuyerData] 구매 데이터 충전 중 오류 발생");
            throw new InternalServerException(ErrorCode.BUYER_DATA_CHARGE_FAIL);
        }
    }

    /**
     * 사용자 ID로 사용자 데이터를 조회하는 내부 메서드입니다.
     * 
     * @param userId 조회할 사용자의 ID
     * @return 조회된 사용자 데이터 엔티티
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    private UserData findUserById(Long userId) {
        return userDataRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private UserData findUserDataByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return userDataRepository.findByUserId(user.getUserId()).orElseThrow(UserNotFoundException::new);
    }
}
