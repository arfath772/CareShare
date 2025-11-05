// Product.java
package retouch.project.careNdShare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String category; // old, new

    @Column(nullable = false)
    private String type; // Donate, Exchange, Resell

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // In your Product entity class
    @Column(name = "product_type")
    private String productType;

    // Add condition field - escape with backticks in column name
    @Column(name = "`condition`", nullable = false)
    private String condition = "Good"; // New, Like New, Good, Fair, Poor

    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;

    private String rejectionReason;

    // Constructors
    public Product() {
        this.createdAt = LocalDateTime.now();
    }

    public Product(String name, Double price, String category, String type, String description, String imagePath, User user) {
        this();
        this.name = name;
        this.price = price;
        this.category = category;
        this.type = type;
        this.description = description;
        this.imagePath = imagePath;
        this.user = user;
    }

    // Updated constructor with condition
    public Product(String name, Double price, String category, String type, String description, String imagePath, User user, String condition) {
        this();
        this.name = name;
        this.price = price;
        this.category = category;
        this.type = type;
        this.description = description;
        this.imagePath = imagePath;
        this.user = user;
        this.condition = condition;
    }

    // Getters and Setters
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

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Don't forget getters and setters
    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    // Add condition getter and setter
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}