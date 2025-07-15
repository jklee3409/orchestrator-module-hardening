package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.user_data.AddBuyerDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateSellableDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.DeductSellableDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.GetUserDataStatusRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.AddBuyerDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.DeductSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;

public interface UserDataService {
    void createUserData(CreateUserDataRequestDto createUserDataRequestDto);
    GetUserDataStatusResponseDto getUserDataStatus(GetUserDataStatusRequestDto getUserDataStatusRequestDto);
    CreateSellableDataResponseDto createSellableData(CreateSellableDataRequestDto createSellableDataRequestDto);
    DeductSellableDataResponseDto deductSellableData(DeductSellableDataRequestDto deductSellableDataRequestDto);
    AddBuyerDataResponseDto chargeBuyerData(AddBuyerDataRequestDto addBuyerDataRequestDto);
}
