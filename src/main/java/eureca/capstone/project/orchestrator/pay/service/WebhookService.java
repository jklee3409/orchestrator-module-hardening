package eureca.capstone.project.orchestrator.pay.service;

import java.util.Map;

public interface WebhookService {
    void processPaymentStatusChanged(Map<String, Object> payload);
}
