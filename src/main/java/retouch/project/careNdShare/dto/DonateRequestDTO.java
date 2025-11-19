package retouch.project.careNdShare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DonateRequestDTO {
    @NotNull(message = "Donation Item ID is required")
    private Long donationId;

    // receiverUserId will be taken from the authenticated user
}