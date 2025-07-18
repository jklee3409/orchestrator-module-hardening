package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.user.entity.User;

public interface UserPayService {
    void charge(User user, Long amount);
}
