package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.dto.VerificationRequest;
import net.farmtocloud.app.entity.Verification;
import net.farmtocloud.app.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/{orderId}")
    public ResponseEntity<ApiResponse> verifyOrder(@PathVariable String orderId,
                                                    @Valid @RequestBody VerificationRequest request) {
        try {
            Verification verification = verificationService.verifyOrder(orderId, request);
            return ResponseEntity.ok(ApiResponse.success("Order verified", verification));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getVerification(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Verification details",
                    verificationService.getVerificationByOrderId(orderId)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
