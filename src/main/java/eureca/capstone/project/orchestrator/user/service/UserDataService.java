package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.UpdateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.AddBuyerDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.DeductSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;

public interface UserDataService {
    void createUserData(CreateUserDataRequestDto createUserDataRequestDto);
    GetUserDataStatusResponseDto getUserDataStatus(String email);
    CreateSellableDataResponseDto createSellableData(String email, UpdateUserDataRequestDto updateUserDataRequestDto);
    DeductSellableDataResponseDto deductSellableData(Long userId, Integer amount);
    AddBuyerDataResponseDto chargeBuyerData(Long userId, Integer amount);
}
