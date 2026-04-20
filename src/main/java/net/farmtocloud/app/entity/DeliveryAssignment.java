package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "delivery_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAssignment {

    @Id
    private String id;

    private String orderId;
    private String partnerName;
    private String partnerPhone;

    /**
     * Statuses: ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED
     */
    @Builder.Default
    private String status = "ASSIGNED";

    private LocalDateTime assignedAt;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
}
