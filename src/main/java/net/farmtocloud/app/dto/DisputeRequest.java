package net.farmtocloud.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String reason;

    private String imageUrl;
    private String comment;
}
