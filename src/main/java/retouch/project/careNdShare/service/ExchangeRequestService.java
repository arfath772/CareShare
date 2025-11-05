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
import java.util.Optional;
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
        // Use the repository method that already handles eager loading
        if (status != null && !status.equals("all")) {
            return exchangeRequestRepository.findAllWithUsersAndProducts(status);
        }
        return exchangeRequestRepository.findAllWithUsersAndProducts(null);
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

    // Add the missing findById method
    public Optional<ExchangeRequest> findById(Long id) {
        return exchangeRequestRepository.findById(id);
    }

    // Add save method if not already present (it seems to be used in your controller)
    public ExchangeRequest save(ExchangeRequest exchangeRequest) {
        return exchangeRequestRepository.save(exchangeRequest);
    }
}