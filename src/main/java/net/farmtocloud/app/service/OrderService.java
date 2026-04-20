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
    private net.farmtocloud.app.repository.NotificationRepository notificationRepository;

    @Autowired
    private CropListingService cropListingService;

    @Autowired
    private UserService userService;

    @Autowired
    private IntelligenceService intelligenceService;

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
        
        notificationRepository.save(net.farmtocloud.app.entity.Notification.builder()
            .userId(listing.getFarmerId())
            .message("New order arrived from " + order.getKitchenName() + " for " + request.getQuantity() + "kg of " + listing.getCropName())
            .type("ORDER_ARRIVED")
            .build());
            
        log.info("Order placed: {} by kitchen {}", saved.getId(), kitchenId);
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

    public List<Order> getDeliveryOrders() {
        return orderRepository.findByStatusIn(java.util.Arrays.asList(
                "PICKUP_ASSIGNED", "VERIFIED", "FARMER_CONFIRMED", "IN_TRANSIT"));
    }

    public Order updateStatus(String orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        if ("DELIVERED".equals(newStatus)) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        Order saved = orderRepository.save(order);
        
        notificationRepository.save(net.farmtocloud.app.entity.Notification.builder()
            .userId(order.getKitchenId())
            .message("Order for " + order.getCropName() + " status updated to " + newStatus)
            .type("STATUS_UPDATE")
            .build());
            
        log.info("Order {} status updated to {}", orderId, newStatus);
        return saved;
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
        
        String msg = confirmed ? "Farmer confirmed order for " + order.getCropName() : "Farmer rejected order for " + order.getCropName() + " (Reason: " + reason + ")";
        notificationRepository.save(net.farmtocloud.app.entity.Notification.builder()
            .userId(order.getKitchenId())
            .message(msg)
            .type("STATUS_UPDATE")
            .build());
            
        return saved;
    }

    public long countByFarmer(String farmerId) {
        return orderRepository.countByFarmerId(farmerId);
    }

    public long countByKitchen(String kitchenId) {
        return orderRepository.countByKitchenId(kitchenId);
    }
}
