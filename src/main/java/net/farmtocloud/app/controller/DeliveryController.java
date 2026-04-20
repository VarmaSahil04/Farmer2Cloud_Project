package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.entity.DeliveryAssignment;
import net.farmtocloud.app.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/assign/{orderId}")
    public ResponseEntity<ApiResponse> assignDelivery(@PathVariable String orderId) {
        try {
            DeliveryAssignment assignment = deliveryService.assignDelivery(orderId);
            return ResponseEntity.ok(ApiResponse.success("Delivery assigned", assignment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable String id,
                                                     @RequestBody Map<String, String> body) {
        try {
            DeliveryAssignment updated = deliveryService.updateDeliveryStatus(id, body.get("status"));
            return ResponseEntity.ok(ApiResponse.success("Delivery status updated", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getByOrderId(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Delivery details",
                    deliveryService.getByOrderId(orderId)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
