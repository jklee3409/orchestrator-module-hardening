package eureca.capstone.project.orchestrator.user.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.UpdateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.AddBuyerDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.DeductSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataServiceImplTest {

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDataServiceImpl userDataService;

    private UserData userData;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 객체 초기화
        userData = UserData.builder()
                .userDataId(1L)
                .userId(1L)
                .planId(1L)
                .totalDataMb(10000)
                .sellableDataMb(2000)
                .buyerDataMb(1000)
                .resetDataAt(1)
                .build();
    }

    @Test
    @DisplayName("사용자 데이터 생성 성공")
    void createUserData_Success() {
        // Given
        CreateUserDataRequestDto requestDto = CreateUserDataRequestDto.builder()
                .userId(1L)
                .planId(1L)
                .monthlyDataMb(10000)
                .resetDataAt(1)
                .build();

        when(userDataRepository.save(any(UserData.class))).thenReturn(userData);

        // When
        userDataService.createUserData(requestDto);

        // Then
        verify(userDataRepository).save(any(UserData.class));
    }

    @Test
    @DisplayName("사용자 데이터 현황 조회 성공")
    void getUserDataStatus_Success() {
        // Given
        String email = "test@example.com";

        // Mock User repository
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(1L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        when(userDataRepository.findByUserId(anyLong())).thenReturn(Optional.of(userData));

        // When
        GetUserDataStatusResponseDto responseDto = userDataService.getUserDataStatus(email);

        // Then
        assertNotNull(responseDto);
        assertEquals(userData.getTotalDataMb(), responseDto.getTotalDataMb());
        assertEquals(userData.getSellableDataMb(), responseDto.getSellableDataMb());
        assertEquals(userData.getBuyerDataMb(), responseDto.getBuyerDataMb());
        verify(userRepository).findByEmail(email);
        verify(userDataRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 데이터 조회 시 예외 발생")
    void getUserDataStatus_UserNotFound_ThrowsException() {
        // Given
        String email = "test@example.com";

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userDataService.getUserDataStatus(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("보유 데이터에서 판매 가능한 데이터로 전환 성공")
    void createSellableData_Success() {
        // Given
        String email = "test@example.com";
        UpdateUserDataRequestDto requestDto = UpdateUserDataRequestDto.builder()
                .amount(1000)
                .build();

        // Mock User repository
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(1L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        when(userDataRepository.findByUserId(anyLong())).thenReturn(Optional.of(userData));

        // When
        CreateSellableDataResponseDto responseDto = userDataService.createSellableData(email, requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(userData.getUserId(), responseDto.getUserId());
        assertEquals(userData.getTotalDataMb(), responseDto.getTotalDataMb());
        assertEquals(userData.getSellableDataMb(), responseDto.getSellableDataMb());
        verify(userRepository).findByEmail(email);
        verify(userDataRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("보유 데이터가 부족한 경우 예외 발생")
    void createSellableData_InsufficientTotalData_ThrowsException() {
        // Given
        String email = "test@example.com";
        UpdateUserDataRequestDto requestDto = UpdateUserDataRequestDto.builder()
                .amount(20000) // 보유 데이터보다 큰 값
                .build();

        // Mock User repository
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(1L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        when(userDataRepository.findByUserId(anyLong())).thenReturn(Optional.of(userData));

        // When & Then
        assertThrows(InternalServerException.class, () -> userDataService.createSellableData(email, requestDto));
        verify(userRepository).findByEmail(email);
        verify(userDataRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("판매 가능한 데이터 차감 성공")
    void deductSellableData_Success() {
        // Given
        UpdateUserDataRequestDto requestDto = UpdateUserDataRequestDto.builder()
                .userId(1L)
                .amount(1000)
                .build();

        when(userDataRepository.findByUserId(anyLong())).thenReturn(Optional.of(userData));

        // When
        DeductSellableDataResponseDto responseDto = userDataService.deductSellableData(requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(userData.getUserId(), responseDto.getUserId());
        assertEquals(userData.getSellableDataMb(), responseDto.getSellableDataMb());
        verify(userDataRepository).findByUserId(requestDto.getUserId());
    }

    @Test
    @DisplayName("판매 가능한 데이터가 부족한 경우 예외 발생")
    void deductSellableData_InsufficientSellableData_ThrowsException() {
        // Given
        UpdateUserDataRequestDto requestDto = UpdateUserDataRequestDto.builder()
                .userId(1L)
                .amount(3000) // 판매 가능한 데이터보다 큰 값
                .build();

        when(userDataRepository.findByUserId(anyLong())).thenReturn(Optional.of(userData));

        // When & Then
        assertThrows(InternalServerException.class, () -> userDataService.deductSellableData(requestDto));
        verify(userDataRepository).findByUserId(requestDto.getUserId());
    }

    @Test
    @DisplayName("구매 데이터 충전 성공")
    void chargeBuyerData_Success() {
        // Given
        UpdateUserDataRequestDto requestDto = UpdateUserDataRequestDto.builder()
                .userId(1L)
                .amount(1000)
                .build();

        when(userDataRepository.findByUserId(anyLong())).thenReturn(Optional.of(userData));

        // When
        AddBuyerDataResponseDto responseDto = userDataService.chargeBuyerData(requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(userData.getUserId(), responseDto.getUserId());
        assertEquals(userData.getBuyerDataMb(), responseDto.getBuyerDataMb());
        verify(userDataRepository).findByUserId(requestDto.getUserId());
    }
}
