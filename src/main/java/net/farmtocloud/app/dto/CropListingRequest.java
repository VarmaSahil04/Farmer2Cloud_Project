package net.farmtocloud.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropListingRequest {

    @NotBlank
    private String cropName;

    @Positive
    private Double quantity;

    @Positive
    private Double pricePerKg;

    private Double marketPrice;
    private LocalDate availableDate;
    private String imageUrl;
}
