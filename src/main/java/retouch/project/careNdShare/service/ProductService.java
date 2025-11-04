package retouch.project.careNdShare.service;

import retouch.project.careNdShare.dto.ProductResponseDTO;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.ProductStatus;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public Product addProduct(Product product, MultipartFile imageFile, User user) throws IOException {
        // Handle image upload
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
        // Create uploads directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        return "/" + UPLOAD_DIR + fileName;
    }

    public List<Product> getUserProducts(Long userId) {
        return productRepository.findByUserId(userId);
    }

    public List<Product> getPendingProducts() {
        return productRepository.findByStatus(ProductStatus.PENDING);
    }

    public List<Product> getUserProductsByStatus(Long userId, ProductStatus status) {
        return productRepository.findByUserIdAndStatus(userId, status);
    }

    public Product approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStatus(ProductStatus.APPROVED);
        product.setApprovedAt(LocalDateTime.now());
        product.setRejectionReason(null);
// Before saving the product
        product.setProductType("your_product_type_value"); // e.g., "PHYSICAL", "DIGITAL", etc.
        productRepository.save(product);
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

    public long getPendingProductsCount() {
        return productRepository.countPendingProducts();
    }

    public long getApprovedProductsCount() {
        return productRepository.countApprovedProducts();
    }
    // Add this method to ProductService.java
    // Add these methods to ProductService.java (replace the existing ones if they exist)
    public List<ProductResponseDTO> getPendingProductsDTO() {
        List<Product> pendingProducts = productRepository.findByStatus(ProductStatus.PENDING);
        return pendingProducts.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getUserProductsDTO(Long userId) {
        List<Product> userProducts = productRepository.findByUserId(userId);
        return userProducts.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }


}