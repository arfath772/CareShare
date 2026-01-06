package retouch.project.careNdShare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DonateRequestDTO {
    @NotNull(message = "Donation Item ID is required")
    private Long donationId;

    @NotBlank(message = "Description is required")
    private String description;

    // receiverUserId will be taken from the authenticated user
}