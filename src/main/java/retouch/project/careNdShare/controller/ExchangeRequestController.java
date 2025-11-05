package retouch.project.careNdShare.controller;

import retouch.project.careNdShare.entity.ExchangeRequest;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.service.ExchangeRequestService;
import retouch.project.careNdShare.service.UserService;
import retouch.project.careNdShare.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-requests")
public class ExchangeRequestController {

    @Autowired
    private ExchangeRequestService exchangeRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitExchangeRequest(
            @RequestParam("targetProductId") Long targetProductId,
            @RequestParam("itemName") String itemName,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "additionalMessage", required = false) String additionalMessage,
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {

        try {
            // Get current user
            String username = authentication.getName();
            User user = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate required fields
            if (itemName == null || itemName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Item name is required"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Category is required"));
            }
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Description is required"));
            }
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Image is required"));
            }

            // Submit exchange request
            ExchangeRequest exchangeRequest = exchangeRequestService.submitExchangeRequest(
                    targetProductId, itemName, category, description,
                    additionalMessage, image, user);

            // Get emails for notification
            String ownerEmail = exchangeRequest.getRequestedProduct().getUser().getEmail();
            String requesterEmail = exchangeRequest.getRequester().getEmail();

            // Send exchange request notifications
            emailService.sendExchangeRequestNotifications(exchangeRequest, ownerEmail, requesterEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange request submitted successfully");
            response.put("exchangeRequest", exchangeRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.badRequest().body(createErrorResponse("Error submitting exchange request: " + e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyExchangeRequests(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ExchangeRequest> requests = exchangeRequestService.getUserExchangeRequests(user.getId(), status);
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error fetching exchange requests: " + e.getMessage()));
        }
    }

    // Owner accept exchange request
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptExchangeRequest(@PathVariable Long id, Authentication authentication) {
        try {
            // Verify the current user is the owner of the requested product
            String username = authentication.getName();
            User currentUser = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ExchangeRequest exchangeRequest = exchangeRequestService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Exchange request not found"));

            // Check if current user is the owner of the requested product
            if (!exchangeRequest.getRequestedProduct().getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(createErrorResponse("You are not authorized to accept this exchange request"));
            }

            // Update status
            exchangeRequest.setStatus("APPROVED");
            ExchangeRequest updatedRequest = exchangeRequestService.save(exchangeRequest);

            // Get emails for notification
            String ownerEmail = exchangeRequest.getRequestedProduct().getUser().getEmail();
            String requesterEmail = exchangeRequest.getRequester().getEmail();

            // Send status update notifications
            emailService.sendExchangeStatusUpdate(updatedRequest, ownerEmail, requesterEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange request accepted successfully");
            response.put("exchangeRequest", updatedRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error accepting exchange request: " + e.getMessage()));
        }
    }

    // Owner decline exchange request
    @PutMapping("/{id}/decline")
    public ResponseEntity<?> declineExchangeRequest(@PathVariable Long id,
                                                    @RequestBody(required = false) Map<String, String> request,
                                                    Authentication authentication) {
        try {
            // Verify the current user is the owner of the requested product
            String username = authentication.getName();
            User currentUser = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ExchangeRequest exchangeRequest = exchangeRequestService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Exchange request not found"));

            // Check if current user is the owner of the requested product
            if (!exchangeRequest.getRequestedProduct().getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(createErrorResponse("You are not authorized to decline this exchange request"));
            }

            String rejectionReason = request != null ? request.get("rejectionReason") : "No reason provided";

            // Update status
            exchangeRequest.setStatus("REJECTED");
            if (rejectionReason != null) {
                // You might want to add a rejectionReason field to your ExchangeRequest entity
                // exchangeRequest.setRejectionReason(rejectionReason);
            }
            ExchangeRequest updatedRequest = exchangeRequestService.save(exchangeRequest);

            // Get emails for notification
            String ownerEmail = exchangeRequest.getRequestedProduct().getUser().getEmail();
            String requesterEmail = exchangeRequest.getRequester().getEmail();

            // Send status update notifications
            emailService.sendExchangeStatusUpdate(updatedRequest, ownerEmail, requesterEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange request declined successfully");
            response.put("exchangeRequest", updatedRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error declining exchange request: " + e.getMessage()));
        }
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}