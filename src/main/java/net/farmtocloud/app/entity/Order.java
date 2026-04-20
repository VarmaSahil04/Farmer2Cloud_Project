package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    private String kitchenId;
    private String kitchenName;
    private String farmerId;
    private String farmerName;
    private String cropListingId;
    private String cropName;

    private Double quantity; // in kg
    private Double unitPrice; // price per kg
    private Double totalPrice;

    /**
     * Order lifecycle statuses:
     * PENDING -> PICKUP_ASSIGNED -> VERIFIED -> FARMER_CONFIRMED -> IN_TRANSIT -> DELIVERED -> CANCELLED
     */
    @Builder.Default
    private String status = "PENDING";

    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;
}
