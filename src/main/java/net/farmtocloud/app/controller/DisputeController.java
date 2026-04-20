package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.dto.DisputeRequest;
import net.farmtocloud.app.entity.Dispute;
import net.farmtocloud.app.service.DisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    @Autowired
    private DisputeService disputeService;

    @PostMapping
    public ResponseEntity<ApiResponse> raiseDispute(Authentication auth,
                                                     @Valid @RequestBody DisputeRequest request) {
        try {
            Dispute dispute = disputeService.raiseDispute(auth.getName(), request);
            return ResponseEntity.ok(ApiResponse.success("Dispute raised", dispute));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDispute(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Dispute details",
                    disputeService.getDisputeById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyDisputes(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("My disputes",
                disputeService.getDisputesByUser(auth.getName())));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getDisputesByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order disputes",
                disputeService.getDisputesByOrder(orderId)));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse> resolveDispute(@PathVariable String id,
                                                       @RequestBody Map<String, String> body) {
        try {
            Dispute resolved = disputeService.resolveDispute(id, body.get("resolution"));
            return ResponseEntity.ok(ApiResponse.success("Dispute resolved", resolved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
