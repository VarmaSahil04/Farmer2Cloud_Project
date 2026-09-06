package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @NotBlank
    private String name;

    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String password;

    private String phone;

    @NotBlank
    private String role; // FARMER or KITCHEN

    // Farmer-specific fields
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> cropTypes;
    private String upiId;
    private String bankDetails;

    // Kitchen-specific fields
    private String businessName;
    private String address;
    private String dailyRequirements;

    // Trust & rating
    @Builder.Default
    private Double trustScore = 0.0;
    @Builder.Default
    private Double deliverySuccessRate = 0.0;
    @Builder.Default
    private Integer totalOrders = 0;
    @Builder.Default
    private Integer successfulDeliveries = 0;
    @Builder.Default
    private Boolean verified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Password reset
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}