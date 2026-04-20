package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    private String id;

    private String orderId;
    private String farmerId;
    private String kitchenId;
    private Double amount;

    /**
     * Statuses: PENDING, PROCESSING, PAID, FAILED
     */
    @Builder.Default
    private String status = "PENDING";

    private String transactionId;
    private String paymentMethod; // UPI, BANK_TRANSFER

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
