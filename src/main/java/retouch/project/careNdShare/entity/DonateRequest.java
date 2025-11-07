package retouch.project.careNdShare.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "donate_request")
public class DonateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private String donationType;
    private Long donationId;
    private Long receiverUserId;
    private String status;
    private LocalDateTime requestedDate;

    public DonateRequest() {}

    public DonateRequest(String donationType, Long donationId, Long receiverUserId, String status, LocalDateTime requestedDate) {
        this.donationType = donationType;
        this.donationId = donationId;
        this.receiverUserId = receiverUserId;
        this.status = status;
        this.requestedDate = requestedDate;
    }

    // Getters & Setters
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getDonationType() { return donationType; }
    public void setDonationType(String donationType) { this.donationType = donationType; }
    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }
    public Long getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(Long receiverUserId) { this.receiverUserId = receiverUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
}


