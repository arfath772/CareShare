package retouch.project.careNdShare.entity;

public enum DonateRequestStatus {
    PENDING,  // User has requested, waiting for admin approval
    APPROVED, // Admin approved the request
    REJECTED  // Admin rejected the request
}