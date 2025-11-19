package retouch.project.careNdShare.entity;

public enum DonationStatus {
    PENDING,  // Waiting for admin approval
    APPROVED, // Approved by admin, visible to public
    REJECTED, // Rejected by admin
    CLAIMED   // Approved and successfully requested by another user
}