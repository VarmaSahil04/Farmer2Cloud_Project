package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.entity.Payment;
import net.farmtocloud.app.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private net.farmtocloud.app.repository.NotificationRepository notificationRepository;

    public Payment settlePayment(String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (!"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Payment can only be settled after delivery");
        }

        // Check if already paid
        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            if ("PAID".equals(p.getStatus())) {
                throw new RuntimeException("Payment already settled for order: " + orderId);
            }
        });

        Payment payment = Payment.builder()
                .orderId(orderId)
                .farmerId(order.getFarmerId())
                .kitchenId(order.getKitchenId())
                .amount(order.getTotalPrice())
                .status("PROCESSING")
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentMethod("UPI")
                .createdAt(LocalDateTime.now())
                .build();

        // Simulate payment processing
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // Update farmer trust score
        userService.updateTrustScore(order.getFarmerId(), true);

        // Send Notification to Farmer!
        notificationRepository.save(net.farmtocloud.app.entity.Notification.builder()
            .userId(order.getFarmerId())
            .message("💰 Payment Received: " + order.getKitchenName() + " has paid " + saved.getAmount() + " for your " + order.getCropName() + " order.")
            .type("PAYMENT_RECEIVED")
            .build());

        log.info("Payment settled for order {}: ₹{}", orderId, saved.getAmount());
        return saved;
    }

    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No payment found for order: " + orderId));
    }

    public List<Payment> getPaymentsByFarmer(String farmerId) {
        return paymentRepository.findByFarmerId(farmerId);
    }

    public List<Payment> getPaymentsByKitchen(String kitchenId) {
        return paymentRepository.findByKitchenId(kitchenId);
    }
}
