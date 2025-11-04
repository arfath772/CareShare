package retouch.project.careNdShare.controller;

import retouch.project.careNdShare.entity.ExchangeRequest;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.service.ExchangeRequestService;
import retouch.project.careNdShare.service.UserService;
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

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
