package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    private String id;

    private String orderId;
    private String raisedBy; // userId
    private String raisedByRole; // FARMER or KITCHEN
    private String reason;
    private String imageUrl;
    private String comment;

    /**
     * Statuses: OPEN, UNDER_REVIEW, RESOLVED
     */
    @Builder.Default
    private String status = "OPEN";

    private String resolution;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
