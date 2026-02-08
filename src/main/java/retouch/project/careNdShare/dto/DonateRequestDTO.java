package retouch.project.careNdShare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
// import lombok.Data; // We don't need this if we write the methods manually

public class DonateRequestDTO {
    @NotNull(message = "Donation Item ID is required")
    private Long donationId;

    @NotBlank(message = "Description is required")
    private String description;

    // --- MANUAL GETTERS AND SETTERS ---
    // These are what @Data was supposed to generate for you

    public Long getDonationId() {
        return donationId;
    }

    public void setDonationId(Long donationId) {
        this.donationId = donationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}