package net.farmtocloud.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String phone;

    @NotBlank
    private String role; // FARMER or KITCHEN

    // Farmer fields
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> cropTypes;
    private String upiId;
    private String bankDetails;

    // Kitchen fields
    private String businessName;
    private String address;
    private String dailyRequirements;
}
