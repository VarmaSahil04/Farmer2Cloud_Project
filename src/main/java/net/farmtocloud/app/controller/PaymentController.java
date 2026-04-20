package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.entity.Payment;
import net.farmtocloud.app.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/settle/{orderId}")
    public ResponseEntity<ApiResponse> settlePayment(@PathVariable String orderId) {
        try {
            Payment payment = paymentService.settlePayment(orderId);
            return ResponseEntity.ok(ApiResponse.success("Payment settled", payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getPaymentByOrder(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Payment details",
                    paymentService.getPaymentByOrderId(orderId)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/farmer")
    public ResponseEntity<ApiResponse> getFarmerPayments(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Farmer payments",
                paymentService.getPaymentsByFarmer(auth.getName())));
    }

    @GetMapping("/kitchen")
    public ResponseEntity<ApiResponse> getKitchenPayments(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Kitchen payments",
                paymentService.getPaymentsByKitchen(auth.getName())));
    }
}
