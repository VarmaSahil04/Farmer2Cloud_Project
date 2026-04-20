package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.dto.VerificationRequest;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.entity.Verification;
import net.farmtocloud.app.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class VerificationService {

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private OrderService orderService;

    public Verification verifyOrder(String orderId, VerificationRequest request) {
        Order order = orderService.getOrderById(orderId);
        if (!"PICKUP_ASSIGNED".equals(order.getStatus())) {
            throw new RuntimeException("Order must be in PICKUP_ASSIGNED status for verification");
        }

        Verification verification = Verification.builder()
                .orderId(orderId)
                .verifiedWeight(request.getVerifiedWeight())
                .imageProofUrl(request.getImageProofUrl())
                .qualityNotes(request.getQualityNotes())
                .verifiedBy(request.getVerifiedBy())
                .verifiedAt(LocalDateTime.now())
                .build();

        Verification saved = verificationRepository.save(verification);

        // Update order status to VERIFIED
        orderService.updateStatus(orderId, "VERIFIED");

        log.info("Order {} verified: weight={} kg", orderId, request.getVerifiedWeight());
        return saved;
    }

    public Verification getVerificationByOrderId(String orderId) {
        return verificationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No verification found for order: " + orderId));
    }
}
