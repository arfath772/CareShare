package retouch.project.careNdShare.service;

import retouch.project.careNdShare.entity.ExchangeRequest;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.repository.ExchangeRequestRepository;
import retouch.project.careNdShare.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExchangeRequestService {

    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;

    @Autowired
    private ProductRepository productRepository;

    private final String UPLOAD_DIR = "uploads/exchange-items/";

    public ExchangeRequest submitExchangeRequest(Long targetProductId, String itemName, String category,
                                                 String description, String additionalMessage,
                                                 MultipartFile image, User user) throws IOException {

        // Validate target product exists
        var targetProduct = productRepository.findById(targetProductId)
                .orElseThrow(() -> new RuntimeException("Target product not found"));

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Save image file
        Files.copy(image.getInputStream(), filePath);

        // Create exchange request
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setTargetProduct(targetProduct);
        exchangeRequest.setExchangeItemName(itemName);
        exchangeRequest.setExchangeItemCategory(category);
        exchangeRequest.setExchangeItemDescription(description);
        exchangeRequest.setExchangeItemImage("/" + UPLOAD_DIR + fileName);
        exchangeRequest.setAdditionalMessage(additionalMessage);
        exchangeRequest.setRequester(user);
        exchangeRequest.setStatus("PENDING");

        return exchangeRequestRepository.save(exchangeRequest);
    }

    public List<ExchangeRequest> getUserExchangeRequests(Long userId, String status) {
        if (status != null && !status.equals("all")) {
            return exchangeRequestRepository.findByRequesterIdAndStatus(userId, status);
        }
        return exchangeRequestRepository.findByRequesterId(userId);
    }

    public List<ExchangeRequest> getAllExchangeRequests(String status) {
        List<ExchangeRequest> exchangeRequests;

        if (status != null && !status.equals("all")) {
            exchangeRequests = exchangeRequestRepository.findByStatus(status);
        } else {
            exchangeRequests = exchangeRequestRepository.findAll();
        }

        // Force initialization of lazy-loaded relationships to prevent JSON serialization issues
        for (ExchangeRequest request : exchangeRequests) {
            // This forces Hibernate to load the relationships immediately
            if (request.getRequester() != null) {
                request.getRequester().getFirstName(); // Access field to force load
                request.getRequester().getEmail();
            }
            if (request.getTargetProduct() != null) {
                request.getTargetProduct().getName(); // Access field to force load
                if (request.getTargetProduct().getUser() != null) {
                    request.getTargetProduct().getUser().getFirstName(); // Access field to force load
                }
            }
        }

        return exchangeRequests;
    }

    public long getExchangeRequestCount(String status) {
        if (status != null && !status.equals("all")) {
            return exchangeRequestRepository.countByStatus(status);
        }
        return exchangeRequestRepository.count();
    }

    public ExchangeRequest approveExchangeRequest(Long id) {
        ExchangeRequest request = exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange request not found"));
        request.setStatus("APPROVED");
        return exchangeRequestRepository.save(request);
    }

    public ExchangeRequest rejectExchangeRequest(Long id, String rejectionReason) {
        ExchangeRequest request = exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange request not found"));
        request.setStatus("REJECTED");
        request.setRejectionReason(rejectionReason);
        return exchangeRequestRepository.save(request);
    }

    public void deleteExchangeRequest(Long id) {
        ExchangeRequest request = exchangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange request not found"));
        exchangeRequestRepository.delete(request);
    }

    public Map<String, Object> getExchangeRequestStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", exchangeRequestRepository.countByStatus("PENDING"));
        stats.put("approved", exchangeRequestRepository.countByStatus("APPROVED"));
        stats.put("rejected", exchangeRequestRepository.countByStatus("REJECTED"));
        stats.put("total", exchangeRequestRepository.count());
        return stats;
    }

    public List<ExchangeRequest> getPendingExchangeRequests() {
        return exchangeRequestRepository.findByStatus("PENDING");
    }
}