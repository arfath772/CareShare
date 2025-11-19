package retouch.project.careNdShare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional; // ❗ ADDED IMPORT
import retouch.project.careNdShare.dto.ProductResponseDTO;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.ProductStatus;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private final String UPLOAD_DIR = "uploads/";

    // --- User Facing Methods ---

    public Product addProduct(Product product, MultipartFile imageFile, User user) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            product.setImagePath(imagePath);
        }
        product.setUser(user);
        product.setStatus(ProductStatus.PENDING);
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);
        return "/" + UPLOAD_DIR + fileName;
    }

    // --- Admin/Read Methods ---

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getPendingProductsDTO() {
        // Uses the eager fetch query for Admin view
        return productRepository.findByStatusWithUser(ProductStatus.PENDING).stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ❗ FIX: Changed logic to use the new eager fetch query
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getUserProductsDTO(Long userId) {
        // Fetches product list eagerly by User ID
        return productRepository.findByUserIdWithUser(userId).stream() // ❗ NEW REPOSITORY METHOD
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<Product> getUserProductsByStatus(Long userId, ProductStatus status) {
        return productRepository.findByUserIdAndStatus(userId, status);
    }

    // --- Stats Methods ---

    public long getPendingProductsCount() {
        return productRepository.countPendingProducts();
    }

    public long getApprovedProductsCount() {
        return productRepository.countApprovedProducts();
    }

    // --- State Change Methods ---

    public Product approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(ProductStatus.APPROVED);
        product.setApprovedAt(LocalDateTime.now());
        product.setRejectionReason(null); // Ensure reason is cleared
        return productRepository.save(product);
    }

    public Product rejectProduct(Long productId, String rejectionReason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(ProductStatus.REJECTED);
        product.setRejectedAt(LocalDateTime.now());
        product.setRejectionReason(rejectionReason);
        return productRepository.save(product);
    }
}