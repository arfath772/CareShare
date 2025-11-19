package retouch.project.careNdShare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
// --- ❗️ CHANGE 1: CORRECTED ENUM IMPORTS ---
import retouch.project.careNdShare.entity.DonateRequestStatus;
import retouch.project.careNdShare.entity.DonationStatus;
// --- END CHANGE 1 ---
import retouch.project.careNdShare.repository.DonateItemRepository; // ❗️ CHANGE 2: ADDED REPOSITORY IMPORT
import retouch.project.careNdShare.service.DonateItemService;
import retouch.project.careNdShare.service.DonateRequestService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/donations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDonationController {

    @Autowired
    private DonateItemService donateItemService;

    @Autowired
    private DonateRequestService donateRequestService;

    // --- ❗️ CHANGE 2: INJECTED THE REPOSITORY ---
    @Autowired
    private DonateItemRepository donateItemRepository;

    // ================================================
    // 🧺 DONATION ITEM MANAGEMENT (Approve/Reject Items)
    // ================================================

    /**
     * ✅ Get all PENDING donation items for admin approval
     */
    @GetMapping("/items/pending")
    public ResponseEntity<?> getPendingDonatedItems() {
        try {
            return ResponseEntity.ok(donateItemService.getPendingDonations());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Get ALL donation items (for viewing all records)
     */
    @GetMapping("/items/all")
    public ResponseEntity<?> getAllDonatedItems() {
        try {
            return ResponseEntity.ok(donateItemService.getAllDonations());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Admin approves a donation item
     */
    @PostMapping("/items/{id}/approve")
    public ResponseEntity<?> approveDonatedItem(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(donateItemService.approveDonation(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Admin rejects a donation item
     * Requires a 'reason' in the request body
     */
    @PostMapping("/items/{id}/reject")
    public ResponseEntity<?> rejectDonatedItem(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            return ResponseEntity.ok(donateItemService.rejectDonation(id, reason));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====================================================
    // 📦 DONATION REQUEST MANAGEMENT (Approve/Reject Requests)
    // ====================================================

    /**
     * ✅ Get all PENDING donation REQUESTS for admin approval
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingDonationRequests() {
        try {
            return ResponseEntity.ok(donateRequestService.getPendingRequests());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Get ALL donation REQUESTS (for viewing all records)
     */
    @GetMapping("/requests/all")
    public ResponseEntity<?> getAllDonationRequests() {
        try {
            // Pass null to get all statuses
            return ResponseEntity.ok(donateRequestService.getAllRequests(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Admin approves a donation REQUEST
     */
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveDonationRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(donateRequestService.approveRequest(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Admin rejects a donation REQUEST
     * Requires a 'reason' in the request body
     */
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectDonationRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            return ResponseEntity.ok(donateRequestService.rejectRequest(id, reason));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Get stats for the admin dashboard
     * --- ❗️ CHANGE 3: THIS METHOD IS NOW FIXED ---
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDonationStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Use the injected repository and the efficient countByStatus method
            stats.put("pendingItems", donateItemRepository.countByStatus(DonationStatus.PENDING));
            stats.put("approvedItems", donateItemRepository.countByStatus(DonationStatus.APPROVED));
            stats.put("claimedItems", donateItemRepository.countByStatus(DonationStatus.CLAIMED));

            // These calls to the service are correct
            stats.put("pendingRequests", donateRequestService.getRequestCountByStatus(DonateRequestStatus.PENDING));
            stats.put("approvedRequests", donateRequestService.getRequestCountByStatus(DonateRequestStatus.APPROVED));
            stats.put("rejectedRequests", donateRequestService.getRequestCountByStatus(DonateRequestStatus.REJECTED));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}