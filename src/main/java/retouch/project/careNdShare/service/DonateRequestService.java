package retouch.project.careNdShare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retouch.project.careNdShare.dto.DonateRequestDTO;
import retouch.project.careNdShare.dto.DonateRequestResponseDTO;
import retouch.project.careNdShare.entity.DonateItem;
import retouch.project.careNdShare.entity.DonateRequest;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.DonationStatus;
import retouch.project.careNdShare.entity.DonateRequestStatus;
import retouch.project.careNdShare.repository.DonateItemRepository;
import retouch.project.careNdShare.repository.DonateRequestRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonateRequestService {

    @Autowired
    private DonateRequestRepository donateRequestRepository;

    @Autowired
    private DonateItemRepository donateItemRepository;

    @Autowired
    private DonateItemService donateItemService; // Used to claim the item

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService; // Add this line

    // ✅ Create a new donation request
    @Transactional
    public DonateRequestResponseDTO createRequest(DonateRequestDTO dto) {
        User currentUser = authService.getCurrentUser();

        DonateItem item = donateItemRepository.findById(dto.getDonationId())
                .orElseThrow(() -> new RuntimeException("Donation item not found"));

        if (item.getStatus() != DonationStatus.APPROVED) {
            throw new RuntimeException("This item is not available for request.");
        }

        if (item.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot request your own donation.");
        }

        DonateRequest request = new DonateRequest();
        request.setDonateItem(item);
        request.setReceiver(currentUser);
        request.setStatus(DonateRequestStatus.PENDING); // Default status

        // ⚠️ CRITICAL FIX: Set the description from DTO
        request.setDescription(dto.getDescription()); // Add this line

        DonateRequest savedRequest = donateRequestRepository.save(request);
        return new DonateRequestResponseDTO(savedRequest);
    }

    // ✅ Get all donation requests (for admin)
    public List<DonateRequestResponseDTO> getAllRequests(DonateRequestStatus status) {
        List<DonateRequest> requests;
        if (status == null) {
            requests = donateRequestRepository.findAll();
        } else {
            requests = donateRequestRepository.findByStatus(status);
        }
        return requests.stream()
                .map(DonateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get all PENDING requests (for admin)
    public List<DonateRequestResponseDTO> getPendingRequests() {
        return donateRequestRepository.findByStatus(DonateRequestStatus.PENDING).stream()
                .map(DonateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get requests made by the currently logged-in user
    public List<DonateRequestResponseDTO> getMyRequests() {
        User currentUser = authService.getCurrentUser();
        return donateRequestRepository.findByReceiverId(currentUser.getId()).stream()
                .map(DonateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Approve a request (for admin) - UPDATED VERSION
    @Transactional
    public DonateRequestResponseDTO approveRequest(Long requestId) {
        DonateRequest request = donateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != DonateRequestStatus.PENDING) {
            throw new RuntimeException("This request has already been processed.");
        }

        // 1. Approve the request
        request.setStatus(DonateRequestStatus.APPROVED);
        request.setRejectionReason(null);
        DonateRequest savedRequest = donateRequestRepository.save(request);

        // 2. Mark the donation item as CLAIMED
        donateItemService.claimDonation(request.getDonateItem().getId());

        // 3. Reject all other pending requests for this specific item
        List<DonateRequest> otherRequests = donateRequestRepository.findByDonateItemIdAndStatus(
                request.getDonateItem().getId(), DonateRequestStatus.PENDING);

        for (DonateRequest otherReq : otherRequests) {
            otherReq.setStatus(DonateRequestStatus.REJECTED);
            otherReq.setRejectionReason("Item has been claimed by another user.");
            donateRequestRepository.save(otherReq);

            // ✅ ADDED: Send rejection email to the receiver
            try {
                emailService.sendDonationRejectionNotifications(otherReq);
                System.out.println("✅ Rejection email sent for request ID: " + otherReq.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send rejection email for request ID " + otherReq.getId() + ": " + e.getMessage());
            }
        }

        // 4. Send approval emails to donor and receiver
        try {
            emailService.sendDonationApprovalNotifications(savedRequest);
            System.out.println("✅ Donation approval emails sent successfully!");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send donation approval emails: " + e.getMessage());
            // Don't throw the exception - allow the approval to succeed even if email fails
            e.printStackTrace();
        }

        return new DonateRequestResponseDTO(savedRequest);
    }

    // ✅ Reject a request (for admin) - UPDATED VERSION
    @Transactional
    public DonateRequestResponseDTO rejectRequest(Long requestId, String reason) {
        DonateRequest request = donateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != DonateRequestStatus.PENDING) {
            throw new RuntimeException("This request has already been processed.");
        }

        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Rejection reason is required");
        }

        request.setStatus(DonateRequestStatus.REJECTED);
        request.setRejectionReason(reason);
        DonateRequest savedRequest = donateRequestRepository.save(request);

        // ✅ ADDED: Send rejection emails
        try {
            emailService.sendDonationRejectionNotifications(savedRequest);
            System.out.println("✅ Rejection emails sent for request ID: " + savedRequest.getId());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send rejection emails: " + e.getMessage());
        }

        return new DonateRequestResponseDTO(savedRequest);
    }

    // ✅ Get stats for admin dashboard
    public long getRequestCountByStatus(DonateRequestStatus status) {
        if (status == null) return donateRequestRepository.count();
        return donateRequestRepository.countByStatus(status);
    }
}