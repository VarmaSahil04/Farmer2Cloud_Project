package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "demand_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandData {

    @Id
    private String id;

    private String cropName;
    private String region;

    /**
     * Demand levels: HIGH, MEDIUM, LOW
     */
    @Builder.Default
    private String demandLevel = "MEDIUM";

    @Builder.Default
    private Integer orderCount = 0;

    @Builder.Default
    private Double avgPrice = 0.0;

    private LocalDateTime updatedAt;
}
