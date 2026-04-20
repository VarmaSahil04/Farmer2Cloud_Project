package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "crop_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropListing {

    @Id
    private String id;

    @NotBlank
    private String farmerId;

    private String farmerName;
    private String farmerLocation;

    @NotBlank
    private String cropName;

    @Positive
    private Double quantity; // in kg

    @Positive
    private Double pricePerKg;

    private Double marketPrice;
    private Double suggestedFairPrice;

    private LocalDate availableDate;

    @Builder.Default
    private String status = "AVAILABLE"; // AVAILABLE, SOLD_OUT, RESERVED

    @Builder.Default
    private String demandLevel = "MEDIUM"; // HIGH, MEDIUM, LOW

    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
