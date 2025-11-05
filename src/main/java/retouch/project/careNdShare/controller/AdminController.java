package retouch.project.careNdShare.controller;

import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.ExchangeRequest;
import retouch.project.careNdShare.service.ProductService;
import retouch.project.careNdShare.service.UserService;
import retouch.project.careNdShare.service.ExchangeRequestService;
import retouch.project.careNdShare.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ExchangeRequestService exchangeRequestService;

    @Autowired
    private EmailService emailService;

    // User Management Endpoints
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching users: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getTotalUsers());
            stats.put("adminUsers", userService.getAdminUsersCount());
            stats.put("regularUsers", userService.getTotalUsers() - userService.getAdminUsersCount());

            // Add exchange request stats
            stats.put("pendingExchanges", exchangeRequestService.getExchangeRequestCount("PENDING"));
            stats.put("approvedExchanges", exchangeRequestService.getExchangeRequestCount("APPROVED"));
            stats.put("rejectedExchanges", exchangeRequestService.getExchangeRequestCount("REJECTED"));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isAdmin = request.get("isAdmin");
            if (isAdmin == null) {
                throw new RuntimeException("isAdmin field is required");
            }

            User updatedUser = userService.updateUserRole(userId, isAdmin);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User role updated successfully");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error updating user role: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Exchange Request Management Endpoints
    @GetMapping("/exchange-requests")
    public ResponseEntity<?> getExchangeRequests(@RequestParam(required = false) String status) {
        try {
            List<ExchangeRequest> exchangeRequests = exchangeRequestService.getAllExchangeRequests(status);
            return ResponseEntity.ok(exchangeRequests);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching exchange requests: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/exchange-requests/pending")
    public ResponseEntity<?> getPendingExchangeRequests() {
        try {
            List<ExchangeRequest> pendingRequests = exchangeRequestService.getAllExchangeRequests("PENDING");
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching pending exchange requests: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/exchange-requests/count")
    public ResponseEntity<?> getExchangeRequestCount(@RequestParam(required = false) String status) {
        try {
            long count = exchangeRequestService.getExchangeRequestCount(status);
            return ResponseEntity.ok(Collections.singletonMap("count", count));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching exchange request count: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/exchange-requests/{id}/approve")
    public ResponseEntity<?> approveExchangeRequest(@PathVariable Long id) {
        try {
            ExchangeRequest approvedRequest = exchangeRequestService.approveExchangeRequest(id);

            // Get emails for notification
            String ownerEmail = approvedRequest.getRequestedProduct().getUser().getEmail();
            String requesterEmail = approvedRequest.getRequester().getEmail();

            // Send status update notifications
            emailService.sendExchangeStatusUpdate(approvedRequest, ownerEmail, requesterEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange request approved successfully");
            response.put("exchangeRequest", approvedRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error approving exchange request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/exchange-requests/{id}/reject")
    public ResponseEntity<?> rejectExchangeRequest(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String rejectionReason = request.get("rejectionReason");
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new RuntimeException("Rejection reason is required");
            }

            ExchangeRequest rejectedRequest = exchangeRequestService.rejectExchangeRequest(id, rejectionReason);

            // Get emails for notification
            String ownerEmail = rejectedRequest.getRequestedProduct().getUser().getEmail();
            String requesterEmail = rejectedRequest.getRequester().getEmail();

            // Send status update notifications
            emailService.sendExchangeStatusUpdate(rejectedRequest, ownerEmail, requesterEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exchange request rejected successfully");
            response.put("exchangeRequest", rejectedRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error rejecting exchange request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/exchange-requests/{id}")
    public ResponseEntity<?> deleteExchangeRequest(@PathVariable Long id) {
        try {
            exchangeRequestService.deleteExchangeRequest(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Exchange request deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error deleting exchange request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/exchange-requests/stats")
    public ResponseEntity<?> getExchangeRequestStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("pending", exchangeRequestService.getExchangeRequestCount("PENDING"));
            stats.put("approved", exchangeRequestService.getExchangeRequestCount("APPROVED"));
            stats.put("rejected", exchangeRequestService.getExchangeRequestCount("REJECTED"));
            stats.put("total", exchangeRequestService.getExchangeRequestCount(null));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching exchange request stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Product Management Endpoints removed - handled by AdminProductController
}