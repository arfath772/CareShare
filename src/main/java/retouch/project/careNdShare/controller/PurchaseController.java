package retouch.project.careNdShare.controller;

// PurchaseController.java

import retouch.project.careNdShare.dto.PurchaseRequestDto;
import retouch.project.careNdShare.dto.PurchaseResponseDto;
import retouch.project.careNdShare.entity.PurchaseRequest;
import retouch.project.careNdShare.entity.PurchaseStatus;
import retouch.project.careNdShare.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/create")
    public ResponseEntity<?> createPurchase(@Valid @RequestBody PurchaseRequestDto purchaseRequestDTO) {
        try {
            String buyerEmail = getCurrentUserEmail();
            PurchaseResponseDto response = purchaseService.createPurchase(purchaseRequestDTO, buyerEmail);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", response.getMessage());
            responseBody.put("purchase", response);

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<?> getMyPurchases() {
        try {
            String buyerEmail = getCurrentUserEmail();
            List<PurchaseRequest> purchases = purchaseService.getPurchasesByBuyer(buyerEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("purchases", purchases);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/my-sales")
    public ResponseEntity<?> getMySales() {
        try {
            String sellerEmail = getCurrentUserEmail();
            List<PurchaseRequest> sales = purchaseService.getSalesBySeller(sellerEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sales", sales);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{purchaseId}/status")
    public ResponseEntity<?> updatePurchaseStatus(
            @PathVariable Long purchaseId,
            @RequestParam PurchaseStatus status) {
        try {
            String userEmail = getCurrentUserEmail();
            PurchaseRequest updatedPurchase = purchaseService.updatePurchaseStatus(purchaseId, status, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Purchase status updated successfully");
            response.put("purchase", updatedPurchase);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("User not authenticated");
    }
}
