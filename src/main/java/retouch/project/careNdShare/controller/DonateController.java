package retouch.project.careNdShare.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.dto.DonateRequestDTO;
import retouch.project.careNdShare.service.DonateItemService;
import retouch.project.careNdShare.service.DonateRequestService;

@RestController
@RequestMapping("/api/donate")
@CrossOrigin(origins = "*") // Keep your CORS config
public class DonateController {

    @Autowired
    private DonateItemService donateItemService;

    @Autowired
    private DonateRequestService donateRequestService;

    // === DONATION ITEM ENDPOINTS ===

    /**
     * ✅ User submits a new donation item.
     * Uses multipart/form-data
     */
    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public ResponseEntity<?> addDonation(@Valid @ModelAttribute DonateItemDTO dto) {
        try {
            return ResponseEntity.ok(donateItemService.addDonation(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Public endpoint to see all APPROVED donation items.
     * Can be filtered by type (e.g., /api/donate/available?type=book)
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDonations(@RequestParam(required = false) String type) {
        try {
            return ResponseEntity.ok(donateItemService.getAvailableDonations(type));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ User gets their own submitted donations (all statuses)
     */
    @GetMapping("/my-donations")
    public ResponseEntity<?> getMyDonations() {
        try {
            return ResponseEntity.ok(donateItemService.getMyDonations());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ Get details for a single donation item
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDonationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(donateItemService.getDonationById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ User deletes one of their own donations
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDonation(@PathVariable Long id) {
        try {
            donateItemService.deleteDonation(id);
            return ResponseEntity.ok("Donation deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === DONATION REQUEST ENDPOINTS ===

    /**
     * ✅ User requests an available donation item
     */
    @PostMapping("/request")
    public ResponseEntity<?> createRequest(@Valid @RequestBody DonateRequestDTO dto) {
        try {
            return ResponseEntity.ok(donateRequestService.createRequest(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ User sees all requests they have made
     */
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests() {
        try {
            return ResponseEntity.ok(donateRequestService.getMyRequests());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}