package retouch.project.careNdShare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.dto.DonateRequestDTO;
import retouch.project.careNdShare.entity.*;
import retouch.project.careNdShare.service.DonateService;

import java.util.List;

@RestController
@RequestMapping("/api/donate")
@CrossOrigin(origins = "*")
public class DonateController {

    private final DonateService donateService;

    public DonateController(DonateService donateService) {
        this.donateService = donateService;
    }

    @PostMapping("/requests/add")
    public ResponseEntity<?> addDonateRequest(@RequestBody DonateRequestDTO dto) {
        try {
            DonateRequest saved = donateService.createDonateRequest(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to add request: " + e.getMessage());
        }
    }

    // Get all donation requests
    @GetMapping("/requests")
    public ResponseEntity<List<DonateRequest>> getAllRequests() {
        return ResponseEntity.ok(donateService.getAllRequests());
    }
    // =========================
    // Generic Donations
    // =========================
    @PostMapping("/item")
    public ResponseEntity<DonateItem> donateItem(@RequestBody DonateItemDTO dto) {
        return ResponseEntity.ok(donateService.createDonation(dto));
    }

    @GetMapping("/items")
    public ResponseEntity<List<DonateItem>> getAllDonations() {
        return ResponseEntity.ok(donateService.getAllDonations());
    }

    // =========================
    // Donation Requests (NEW)
    // =========================
    @PostMapping("/request")
    public ResponseEntity<DonateRequest> createRequest(@RequestBody DonateRequest request) {
        return ResponseEntity.ok(donateService.createDonateRequest(request));
    }



    // =========================
    // Category Donations
    // =========================

    // Clothes
    @PostMapping("/clothes")
    public ResponseEntity<DonateClothes> donateClothes(@RequestBody DonateClothes item) {
        return ResponseEntity.ok(donateService.donateClothes(item));
    }

    @GetMapping("/clothes")
    public ResponseEntity<List<DonateClothes>> getAllClothes() {
        return ResponseEntity.ok(donateService.getAllClothes());
    }

    // Books
    @PostMapping("/books")
    public ResponseEntity<DonateBooks> donateBooks(@RequestBody DonateBooks item) {
        return ResponseEntity.ok(donateService.donateBooks(item));
    }

    @GetMapping("/books")
    public ResponseEntity<List<DonateBooks>> getAllBooks() {
        return ResponseEntity.ok(donateService.getAllBooks());
    }

    // Toys
    @PostMapping("/toys")
    public ResponseEntity<DonateToys> donateToys(@RequestBody DonateToys item) {
        return ResponseEntity.ok(donateService.donateToys(item));
    }

    @GetMapping("/toys")
    public ResponseEntity<List<DonateToys>> getAllToys() {
        return ResponseEntity.ok(donateService.getAllToys());
    }

    // Other Items
    @PostMapping("/other")
    public ResponseEntity<DonateOtherItems> donateOtherItems(@RequestBody DonateOtherItems item) {
        return ResponseEntity.ok(donateService.donateOtherItems(item));
    }

    @GetMapping("/other")
    public ResponseEntity<List<DonateOtherItems>> getAllOtherItems() {
        return ResponseEntity.ok(donateService.getAllOtherItems());
    }
}


