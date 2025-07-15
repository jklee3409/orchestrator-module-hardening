package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateSellableDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.GetUserDataStatusRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;

public interface UserDataService {
    void createUserData(CreateUserDataRequestDto createUserDataRequestDto);
    GetUserDataStatusResponseDto getUserDataStatus(GetUserDataStatusRequestDto getUserDataStatusRequestDto);
    CreateSellableDataResponseDto createSellableData(CreateSellableDataRequestDto createSellableDataRequestDto);
}
