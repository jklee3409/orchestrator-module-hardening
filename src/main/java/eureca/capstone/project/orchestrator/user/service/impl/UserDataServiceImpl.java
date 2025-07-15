package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.AddBuyerDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateSellableDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.DeductSellableDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.GetUserDataStatusRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.AddBuyerDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateUserDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.DeductSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {

    private final UserDataRepository userDataRepository;

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

    @Override
    public GetUserDataStatusResponseDto getUserDataStatus(GetUserDataStatusRequestDto getUserDataStatusRequestDto) {
        log.info("[getUserDataStatus] 사용자 {} 의 데이터 현황 조회", getUserDataStatusRequestDto.getUserId());

        UserData userData = findUserById(getUserDataStatusRequestDto.getUserId());

        return GetUserDataStatusResponseDto.fromEntity(userData);
    }

    @Override
    @Transactional
    public CreateSellableDataResponseDto createSellableData(CreateSellableDataRequestDto createSellableDataRequestDto) {
        log.info("[createSellableData] 사용자 {} 보유 데이터에서 판매 가능 데이터로 전환", createSellableDataRequestDto.getUserId());
        try {
            UserData userData = findUserById(createSellableDataRequestDto.getUserId());

            if (userData.getTotalDataMb() < createSellableDataRequestDto.getAmount()) {
                throw new InternalServerException(ErrorCode.USER_TOTAL_DATA_LACK);
            }

            userData.createSellableData(createSellableDataRequestDto.getAmount());
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

    @Override
    @Transactional
    public DeductSellableDataResponseDto deductSellableData(DeductSellableDataRequestDto deductSellableDataRequestDto) {
        log.info("[deductSellableData] 사용자 {} 판매 가능데이터 차감", deductSellableDataRequestDto.getUserId());
        try {
            UserData userData = findUserById(deductSellableDataRequestDto.getUserId());

            if (userData.getSellableDataMb() < deductSellableDataRequestDto.getAmount()) {
                throw new InternalServerException(ErrorCode.USER_SELLABLE_DATA_LACK);
            }

            userData.deductSellableData(deductSellableDataRequestDto.getAmount());
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

    @Override
    @Transactional
    public AddBuyerDataResponseDto chargeBuyerData(AddBuyerDataRequestDto addBuyerDataRequestDto) {
        log.info("[chargeBuyerData] 사용자 {} 구매 데이터 충전", addBuyerDataRequestDto.getUserId());
        try {
            UserData userData = findUserById(addBuyerDataRequestDto.getUserId());

            userData.addBuyerData(addBuyerDataRequestDto.getAmount());
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

    private UserData findUserById(Long userId) {
        return userDataRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
