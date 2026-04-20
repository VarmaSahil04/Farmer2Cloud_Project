package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    @Autowired
    private UserService userService;

    @Autowired
    private CropListingService cropListingService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private IntelligenceService intelligenceService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(Authentication auth) {
        String kitchenId = auth.getName();
        User kitchen = userService.getUserById(kitchenId);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("kitchen", kitchen);
        dashboard.put("totalOrders", orderService.countByKitchen(kitchenId));
        dashboard.put("orders", orderService.getOrdersByKitchen(kitchenId));
        dashboard.put("recommendations", intelligenceService.getRecommendations(kitchenId));
        dashboard.put("availableCrops", cropListingService.getAvailableListings());
        dashboard.put("payments", paymentService.getPaymentsByKitchen(kitchenId));

        return ResponseEntity.ok(ApiResponse.success("Kitchen dashboard", dashboard));
    }

    @GetMapping("/browse")
    public ResponseEntity<ApiResponse> browseCrops(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String location) {

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Search results",
                    cropListingService.searchListings(search)));
        }
        if (minPrice != null && maxPrice != null) {
            return ResponseEntity.ok(ApiResponse.success("Price filtered results",
                    cropListingService.filterByPriceRange(minPrice, maxPrice)));
        }
        if (location != null && !location.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Location filtered results",
                    cropListingService.filterByLocation(location)));
        }
        return ResponseEntity.ok(ApiResponse.success("Available crops",
                cropListingService.getAvailableListings()));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse> getRecommendations(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Recommendations",
                intelligenceService.getRecommendations(auth.getName())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(Authentication auth, @RequestBody User updatedData) {
        User updated = userService.updateProfile(auth.getName(), updatedData);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }
}
