package eureca.capstone.project.orchestrator.auth.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.entity.UserAuthority;
import eureca.capstone.project.orchestrator.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.orchestrator.common.exception.custom.BlockUserException;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 혹시 차단된 사용자인지 확인
        if (userService.checkBanUser(email)) {
            log.error("[loadUserByUsername] : checkBanUser throw BlockUserException");
            throw new BlockUserException();
        }

        // 요청 파라미터 로그 출력 및 사용자 정보 추출
        log.info("loadUserByUsername : {}", email);
        UserInformationDto userInformationDto = userRepository.findUserInformation(email);

        // role, authority 를 GrantedAuthority 변환 및 로그 출력
        Set<String> roles = userInformationDto.getRoles();
        Set<String> authorities = userInformationDto.getAuthorities();
        log.info("role : {}", roles);
        log.info("authorities : {}", authorities);

        // 제재 당한 권한이 있는지 확인 및 기존 권한에서 제외 및 로그 출력
        List<UserAuthority> blockUserList = userAuthorityRepository
                .findUserAuthorityByUserId(userInformationDto.getUserId());
        Set<String> blockUserAuthority = blockUserList.stream()
                .map(blockUser -> blockUser.getAuthority().getName())
                .collect(Collectors.toSet());
        authorities.removeAll(blockUserAuthority);
        log.info("blockUserList : {}", blockUserList);
        log.info("blockUserAuthority : {}", blockUserAuthority);
        log.info("authorities : {}", authorities);

        // 권한과 역할을 담을 변수 생성
        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

        // 역할과 권한 추출 및 grantedAuthorities 에 담기
        for (String role : roles) grantedAuthorities.add(new SimpleGrantedAuthority(role));
        for (String authority : authorities) grantedAuthorities.add(new SimpleGrantedAuthority(authority));

        // customUserDetailsDto 반환 객체 생성 및 로그 출력
        CustomUserDetailsDto customUserDetailsDto = CustomUserDetailsDto.builder()
                .userId(userInformationDto.getUserId())
                .email(userInformationDto.getEmail())
                .password(userInformationDto.getPassword())
                .authorities(grantedAuthorities)
                .build();
        log.info("customUserDetailsDto : {}", customUserDetailsDto.toString());

        // return
        return customUserDetailsDto;
    }
}
