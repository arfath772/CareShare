package retouch.project.careNdShare.dto;

import java.time.LocalDateTime;

public class DonateRequestDTO {

    private String donationType;
    private Long donationId;
    private Long receiverUserId;
    private String status;
    private LocalDateTime requestedDate;


    public String getDonationType() {
        return donationType;
    }

    public void setDonationType(String donationType) {
        this.donationType = donationType;
    }

    public Long getDonationId() {
        return donationId;
    }

    public void setDonationId(Long donationId) {
        this.donationId = donationId;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }
}

