package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.entity.DeliveryAssignment;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.repository.DeliveryAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class DeliveryService {

    @Autowired
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Autowired
    private OrderService orderService;

    // Simulated delivery partners
    private static final List<String> DELIVERY_PARTNERS = Arrays.asList(
            "Raju Kumar", "Suresh Patel", "Amit Sharma", "Priya Singh", "Deepak Verma"
    );

    private static final List<String> PARTNER_PHONES = Arrays.asList(
            "9876543210", "9876543211", "9876543212", "9876543213", "9876543214"
    );

    public DeliveryAssignment assignDelivery(String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Order must be in PENDING status for delivery assignment");
        }

        // Simulate random delivery partner assignment
        Random rand = new Random();
        int idx = rand.nextInt(DELIVERY_PARTNERS.size());

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .orderId(orderId)
                .partnerName(DELIVERY_PARTNERS.get(idx))
                .partnerPhone(PARTNER_PHONES.get(idx))
                .status("ASSIGNED")
                .assignedAt(LocalDateTime.now())
                .build();

        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);

        // Update order status
        orderService.updateStatus(orderId, "PICKUP_ASSIGNED");

        log.info("Delivery assigned for order {}: partner {}", orderId, saved.getPartnerName());
        return saved;
    }

    public DeliveryAssignment updateDeliveryStatus(String assignmentId, String status) {
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Delivery assignment not found: " + assignmentId));

        assignment.setStatus(status);
        if ("PICKED_UP".equals(status)) {
            assignment.setPickupTime(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            assignment.setDeliveryTime(LocalDateTime.now());
            orderService.updateStatus(assignment.getOrderId(), "DELIVERED");
        }

        return deliveryAssignmentRepository.save(assignment);
    }

    public DeliveryAssignment getByOrderId(String orderId) {
        return deliveryAssignmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No delivery assignment for order: " + orderId));
    }
}
