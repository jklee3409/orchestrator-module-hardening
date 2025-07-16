package eureca.capstone.project.orchestrator.auth.repository.custom;

import eureca.capstone.project.orchestrator.auth.entity.UserAuthority;

import java.util.List;

public interface UserAuthorityRepositoryCustom {
    List<UserAuthority> findUserAuthorityByUserId(Long userId);
}
