package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface UserPayService {
    UserPay charge(User user, Long amount);
}
