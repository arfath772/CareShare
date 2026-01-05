package retouch.project.careNdShare.dto;

import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.ProductStatus;
import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDTO {
    private Long id;
    private String name;
    private Double price;
    private String category;
    private String type;
    private String description;
    private String imagePath;
    private List<String> imagePaths;
    private ProductStatus status;
    private String condition; // Make sure this is included
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private Long userId;
    private String userName;
    private String userEmail;

    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.category = product.getCategory();
        this.type = product.getType();
        this.description = product.getDescription();
        this.imagePath = product.getImagePath();
        this.imagePaths = product.getImagePaths();
        this.status = product.getStatus();
        this.condition = product.getCondition(); // IMPORTANT: Include condition
        this.createdAt = product.getCreatedAt();
        this.approvedAt = product.getApprovedAt();
        this.rejectedAt = product.getRejectedAt();
        this.rejectionReason = product.getRejectionReason();

        if (product.getUser() != null) {
            this.userId = product.getUser().getId();
            this.userName = product.getUser().getFirstName() + " " + product.getUser().getLastName();
            this.userEmail = product.getUser().getEmail();
        }
    }

    // Getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}