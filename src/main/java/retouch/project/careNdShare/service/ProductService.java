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

    public Product addProduct(Product product, MultipartFile[] images, User user) throws IOException {
        try {
            // DEBUG: Check incoming product
            System.out.println("=== ProductService.addProduct DEBUG START ===");
            System.out.println("Incoming product condition: " + product.getCondition());
            System.out.println("Incoming product productCondition: " + product.getProductCondition());

            // Set basic product info
            product.setUser(user);
            product.setStatus(ProductStatus.PENDING);
            product.setCreatedAt(LocalDateTime.now());

            // Check if condition is properly set
            if (product.getProductCondition() == null || product.getProductCondition().isEmpty()) {
                System.out.println("WARNING: Condition is empty, setting to 'Good'");
                product.setProductCondition("Good");
            } else {
                System.out.println("Condition is set to: " + product.getProductCondition());
            }

            // Set other nullable fields
            product.setApprovedAt(null);
            product.setRejectedAt(null);
            product.setRejectionReason(null);

            // Save product first
            Product savedProduct = productRepository.save(product);
            System.out.println("Saved product ID: " + savedProduct.getId());
            System.out.println("Saved product condition: " + savedProduct.getProductCondition());

            // Handle images
            handleProductImages(savedProduct, images);

            // Save again with images
            Product finalProduct = productRepository.save(savedProduct);
            System.out.println("Final product condition: " + finalProduct.getProductCondition());
            System.out.println("=== ProductService.addProduct DEBUG END ===");

            return finalProduct;
        } catch (Exception e) {
            System.err.println("ERROR in ProductService.addProduct: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
    }

    private void handleProductImages(Product product, MultipartFile[] images) throws IOException {
        if (images != null && images.length > 0) {
            String mainImagePath = null;

            for (int i = 0; i < images.length; i++) {
                if (images[i] != null && !images[i].isEmpty()) {
                    String imagePath = saveImage(images[i], product.getId());

                    if (i == 0) {
                        mainImagePath = imagePath;
                        product.setImagePath(mainImagePath);
                    }
                    product.addImagePath(imagePath);
                }
            }

            if (mainImagePath == null) {
                product.setImagePath("/uploads/default-product.png");
            }
        } else {
            product.setImagePath("/uploads/default-product.png");
        }
    }

    public Product addProduct(Product product, MultipartFile imageFile, User user) throws IOException {
        MultipartFile[] images;
        if (imageFile != null && !imageFile.isEmpty()) {
            images = new MultipartFile[]{imageFile};
        } else {
            images = new MultipartFile[0];
        }
        return addProduct(product, images, user);
    }

    private String saveImage(MultipartFile imageFile, Long productId) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String productDir = UPLOAD_DIR + "product_" + productId + "/";
        Path productPath = Paths.get(productDir);
        if (!Files.exists(productPath)) Files.createDirectories(productPath);

        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = productPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        return "/" + productDir + fileName;
    }

    // All other methods remain the same
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