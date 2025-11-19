package retouch.project.careNdShare.entity;

import jakarta.persistence.*;
import retouch.project.careNdShare.entity.DonateRequestStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "donate_requests")
public class DonateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private DonateItem donateItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonateRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedDate;

    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        requestedDate = LocalDateTime.now();
    }

    // Constructors
    public DonateRequest() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DonateItem getDonateItem() { return donateItem; }
    public void setDonateItem(DonateItem donateItem) { this.donateItem = donateItem; }
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    public DonateRequestStatus getStatus() { return status; }
    public void setStatus(DonateRequestStatus status) { this.status = status; }
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}