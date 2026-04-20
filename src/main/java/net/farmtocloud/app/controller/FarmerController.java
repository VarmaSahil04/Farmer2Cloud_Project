package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.service.OrderService;
import net.farmtocloud.app.service.UserService;
import net.farmtocloud.app.service.IntelligenceService;
import net.farmtocloud.app.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private IntelligenceService intelligenceService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(Authentication auth) {
        String farmerId = auth.getName();
        User farmer = userService.getUserById(farmerId);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("farmer", farmer);
        dashboard.put("totalOrders", orderService.countByFarmer(farmerId));
        dashboard.put("orders", orderService.getOrdersByFarmer(farmerId));
        dashboard.put("trustScore", farmer.getTrustScore());
        dashboard.put("deliverySuccessRate", farmer.getDeliverySuccessRate());
        dashboard.put("verified", farmer.getVerified());
        dashboard.put("demandInsights", intelligenceService.getDemandHeatmap());
        dashboard.put("payments", paymentService.getPaymentsByFarmer(farmerId));

        return ResponseEntity.ok(ApiResponse.success("Farmer dashboard", dashboard));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getOrders(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Farmer orders",
                orderService.getOrdersByFarmer(auth.getName())));
    }

    @GetMapping("/trust-score")
    public ResponseEntity<ApiResponse> getTrustScore(Authentication auth) {
        User farmer = userService.getUserById(auth.getName());
        Map<String, Object> trust = new LinkedHashMap<>();
        trust.put("trustScore", farmer.getTrustScore());
        trust.put("deliverySuccessRate", farmer.getDeliverySuccessRate());
        trust.put("totalOrders", farmer.getTotalOrders());
        trust.put("successfulDeliveries", farmer.getSuccessfulDeliveries());
        trust.put("verified", farmer.getVerified());
        return ResponseEntity.ok(ApiResponse.success("Trust score", trust));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(Authentication auth, @RequestBody User updatedData) {
        User updated = userService.updateProfile(auth.getName(), updatedData);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }
}
