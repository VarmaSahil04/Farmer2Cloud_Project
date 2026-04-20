package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.dto.DisputeRequest;
import net.farmtocloud.app.entity.Dispute;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.repository.DisputeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DisputeService {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private UserService userService;

    public Dispute raiseDispute(String userId, DisputeRequest request) {
        User user = userService.getUserById(userId);

        Dispute dispute = Dispute.builder()
                .orderId(request.getOrderId())
                .raisedBy(userId)
                .raisedByRole(user.getRole())
                .reason(request.getReason())
                .imageUrl(request.getImageUrl())
                .comment(request.getComment())
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        Dispute saved = disputeRepository.save(dispute);
        log.info("Dispute raised for order {} by {}", request.getOrderId(), userId);
        return saved;
    }

    public Dispute resolveDispute(String disputeId, String resolution) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found: " + disputeId));

        dispute.setStatus("RESOLVED");
        dispute.setResolution(resolution);
        dispute.setResolvedAt(LocalDateTime.now());

        log.info("Dispute {} resolved", disputeId);
        return disputeRepository.save(dispute);
    }

    public List<Dispute> getDisputesByOrder(String orderId) {
        return disputeRepository.findByOrderId(orderId);
    }

    public List<Dispute> getDisputesByUser(String userId) {
        return disputeRepository.findByRaisedBy(userId);
    }

    public List<Dispute> getOpenDisputes() {
        return disputeRepository.findByStatus("OPEN");
    }

    public Dispute getDisputeById(String id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispute not found: " + id));
    }
}
