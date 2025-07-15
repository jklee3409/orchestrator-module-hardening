package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateUserDataResponseDto;
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
    public CreateUserDataResponseDto createUserData(CreateUserDataRequestDto createUserDataRequestDto) {
        log.info("[createUserData] 사용자 데이터 등록 요청");

        try {
            log.info("[createUserData] 사용자 데이터 레코드 등록 시작: userId={}", createUserDataRequestDto.getUserId());
            UserData createUserData = userDataRepository.save(CreateUserDataRequestDto.toEntity(createUserDataRequestDto));
            log.info("[createUserData] 사용자 데이터 레코드 등록 완료: userId={}", createUserDataRequestDto.getUserId());

            return CreateUserDataResponseDto.builder()
                    .userDataId(createUserData.getUserDataId())
                    .build();

        } catch (Exception e) {
            log.error("[createUserData] 사용자 데이터 등록 중 오류 발생: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorCode.USER_DATA_CREATE_FAIL);
        }
    }
}
