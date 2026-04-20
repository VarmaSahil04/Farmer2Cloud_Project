package net.farmtocloud.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRequest {

    @Positive
    private Double verifiedWeight;

    private String imageProofUrl;
    private String qualityNotes;
    private String verifiedBy;
}
