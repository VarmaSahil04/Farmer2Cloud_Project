package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    private String id;

    private String orderId;
    private Double verifiedWeight; // in kg
    private String imageProofUrl;
    private String qualityNotes;
    private String verifiedBy; // delivery partner name

    private LocalDateTime verifiedAt;
}
