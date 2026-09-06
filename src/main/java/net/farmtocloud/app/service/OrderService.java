package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.dto.OrderRequest;
import net.farmtocloud.app.entity.CropListing;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CropListingService cropListingService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntelligenceService intelligenceService;

    @Autowired
    private EmailService emailService;

    public Order placeOrder(String kitchenId, OrderRequest request) {
        User kitchen = userService.getUserById(kitchenId);
        CropListing listing = cropListingService.getListingById(request.getCropListingId());

        if (request.getQuantity() > listing.getQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available quantity");
        }

        double totalPrice = request.getQuantity() * listing.getPricePerKg();

        Order order = Order.builder()
                .kitchenId(kitchenId)
                .kitchenName(kitchen.getBusinessName() != null ? kitchen.getBusinessName() : kitchen.getName())
                .farmerId(listing.getFarmerId())
                .farmerName(listing.getFarmerName())
                .cropListingId(listing.getId())
                .cropName(listing.getCropName())
                .quantity(request.getQuantity())
                .unitPrice(listing.getPricePerKg())
                .totalPrice(Math.round(totalPrice * 100.0) / 100.0)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Update listing quantity
        listing.setQuantity(listing.getQuantity() - request.getQuantity());
        if (listing.getQuantity() <= 0) {
            listing.setStatus("SOLD_OUT");
        }
        cropListingService.updateListing(listing.getId(),
                net.farmtocloud.app.dto.CropListingRequest.builder()
                        .cropName(listing.getCropName())
                        .quantity(listing.getQuantity())
                        .pricePerKg(listing.getPricePerKg())
                        .build());

        // Update demand data
        intelligenceService.recordOrder(listing.getCropName(), listing.getFarmerLocation(), listing.getPricePerKg());

        Order saved = orderRepository.save(order);
        log.info("Order placed: {} by kitchen {}", saved.getId(), kitchenId);

        notifyOrderStatusChange(saved);

        return saved;
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByFarmer(String farmerId) {
        return orderRepository.findByFarmerId(farmerId);
    }

    public List<Order> getOrdersByKitchen(String kitchenId) {
        return orderRepository.findByKitchenId(kitchenId);
    }

    /**
     * Orders currently active in the delivery pipeline —
     * assigned for pickup or already in transit.
     */
    public List<Order> getDeliveryOrders() {
        return orderRepository.findByStatusIn(List.of("PICKUP_ASSIGNED", "IN_TRANSIT"));
    }

    public Order updateStatus(String orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        if ("DELIVERED".equals(newStatus)) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, newStatus);

        notifyOrderStatusChange(saved);

        return saved;
    }

    /**
     * Emails both the farmer and the kitchen whenever an order's status changes.
     * Failures here are logged inside EmailService and never propagate —
     * a bad SMTP config should never break an order status update.
     */
    private void notifyOrderStatusChange(Order order) {
        try {
            User farmer = userService.getUserById(order.getFarmerId());
            emailService.sendOrderStatusEmail(order, farmer.getEmail(), farmer.getName());
        } catch (Exception e) {
            log.warn("Could not notify farmer for order {}: {}", order.getId(), e.getMessage());
        }
        try {
            User kitchen = userService.getUserById(order.getKitchenId());
            emailService.sendOrderStatusEmail(order, kitchen.getEmail(), kitchen.getName());
        } catch (Exception e) {
            log.warn("Could not notify kitchen for order {}: {}", order.getId(), e.getMessage());
        }
    }

    public Order farmerConfirm(String orderId, boolean confirmed, String reason) {
        Order order = getOrderById(orderId);
        if (!"VERIFIED".equals(order.getStatus())) {
            throw new RuntimeException("Order must be in VERIFIED status for farmer confirmation");
        }
        if (confirmed) {
            order.setStatus("FARMER_CONFIRMED");
        } else {
            order.setStatus("CANCELLED");
            order.setRejectionReason(reason);
        }
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        notifyOrderStatusChange(saved);

        return saved;
    }

    public long countByFarmer(String farmerId) {
        return orderRepository.countByFarmerId(farmerId);
    }

    public long countByKitchen(String kitchenId) {
        return orderRepository.countByKitchenId(kitchenId);
    }
}