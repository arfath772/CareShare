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
@CrossOrigin(origins = "*")
public class DonateController {

    @Autowired
    private DonateItemService donateItemService;

    @Autowired
    private DonateRequestService donateRequestService;

    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public ResponseEntity<?> addDonation(@Valid @ModelAttribute DonateItemDTO dto) {
        try {
            return ResponseEntity.ok(donateItemService.addDonation(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDonations(@RequestParam(required = false) String type) {
        try {
            return ResponseEntity.ok(donateItemService.getAvailableDonations(type));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-donations")
    public ResponseEntity<?> getMyDonations() {
        try {
            return ResponseEntity.ok(donateItemService.getMyDonations());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDonationById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(donateItemService.getDonationById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ ADD THIS - for View Details feature
    @GetMapping("/details/{id}")
    public ResponseEntity<?> getDonationDetails(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(donateItemService.getDonationById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDonation(@PathVariable Long id) {
        try {
            donateItemService.deleteDonation(id);
            return ResponseEntity.ok("Donation deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> createRequest(@Valid @RequestBody DonateRequestDTO dto) {
        try {
            return ResponseEntity.ok(donateRequestService.createRequest(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests() {
        try {
            return ResponseEntity.ok(donateRequestService.getMyRequests());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}