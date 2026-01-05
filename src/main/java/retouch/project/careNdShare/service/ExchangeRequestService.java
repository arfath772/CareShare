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
import java.util.ArrayList;
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
                                                 List<MultipartFile> images, User user) throws IOException {

        // Validate target product exists
        var targetProduct = productRepository.findById(targetProductId)
                .orElseThrow(() -> new RuntimeException("Target product not found"));

        // Check if user is trying to exchange with their own product
        if (targetProduct.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot exchange with your own product");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save all images
        List<String> savedImageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String imageUrl = saveImage(image);
                savedImageUrls.add(imageUrl);
            }
        }

        // Create exchange request
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        exchangeRequest.setTargetProduct(targetProduct);
        exchangeRequest.setExchangeItemName(itemName);
        exchangeRequest.setExchangeItemCategory(category);
        exchangeRequest.setExchangeItemDescription(description);
        exchangeRequest.setExchangeItemImages(savedImageUrls);
        exchangeRequest.setAdditionalMessage(additionalMessage);
        exchangeRequest.setRequester(user);
        exchangeRequest.setStatus("PENDING");

        return exchangeRequestRepository.save(exchangeRequest);
    }

    private String saveImage(MultipartFile image) throws IOException {
        // Generate unique filename with original extension
        String originalFilename = image.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);

        // Save image file
        Files.copy(image.getInputStream(), filePath);

        // Return relative path for web access
        return "exchange-items/" + fileName;
    }

    public List<ExchangeRequest> getUserExchangeRequests(Long userId, String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
            return exchangeRequestRepository.findByRequesterIdAndStatus(userId, status.toUpperCase());
        }
        return exchangeRequestRepository.findByRequesterId(userId);
    }

    public List<ExchangeRequest> getReceivedExchangeRequests(Long ownerId, String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
            return exchangeRequestRepository.findByTargetProductUserIdAndStatus(ownerId, status.toUpperCase());
        }
        return exchangeRequestRepository.findByTargetProductUserId(ownerId);
    }

    public List<ExchangeRequest> getAllExchangeRequests(String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
            return exchangeRequestRepository.findAllWithUsersAndProducts(status.toUpperCase());
        }
        return exchangeRequestRepository.findAllWithUsersAndProducts(null);
    }

    public long getExchangeRequestCount(String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
            return exchangeRequestRepository.countByStatus(status.toUpperCase());
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

        // Delete associated image files
        if (request.getExchangeItemImages() != null) {
            for (String imageUrl : request.getExchangeItemImages()) {
                try {
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    // Log error but continue with deletion
                    System.err.println("Failed to delete image file: " + imageUrl);
                }
            }
        }

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

    public Optional<ExchangeRequest> findById(Long id) {
        return exchangeRequestRepository.findById(id);
    }

    public ExchangeRequest save(ExchangeRequest exchangeRequest) {
        return exchangeRequestRepository.save(exchangeRequest);
    }
}