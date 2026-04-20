package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.dto.OrderRequest;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse> placeOrder(Authentication auth,
                                                   @Valid @RequestBody OrderRequest request) {
        try {
            Order order = orderService.placeOrder(auth.getName(), request);
            return ResponseEntity.ok(ApiResponse.success("Order placed successfully", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Order details",
                    orderService.getOrderById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/farmer")
    public ResponseEntity<ApiResponse> getFarmerOrders(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Farmer orders",
                orderService.getOrdersByFarmer(auth.getName())));
    }

    @GetMapping("/kitchen")
    public ResponseEntity<ApiResponse> getKitchenOrders(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Kitchen orders",
                orderService.getOrdersByKitchen(auth.getName())));
    }

    @GetMapping("/delivery")
    public ResponseEntity<ApiResponse> getDeliveryOrders(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Delivery orders",
                orderService.getDeliveryOrders()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable String id,
                                                     @RequestBody Map<String, String> body) {
        try {
            Order order = orderService.updateStatus(id, body.get("status"));
            return ResponseEntity.ok(ApiResponse.success("Status updated", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/farmer-confirm")
    public ResponseEntity<ApiResponse> farmerConfirm(@PathVariable String id,
                                                      @RequestBody Map<String, Object> body) {
        try {
            boolean confirmed = (boolean) body.getOrDefault("confirmed", false);
            String reason = (String) body.getOrDefault("reason", "");
            Order order = orderService.farmerConfirm(id, confirmed, reason);
            return ResponseEntity.ok(ApiResponse.success(
                    confirmed ? "Order confirmed" : "Order rejected", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
