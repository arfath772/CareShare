package retouch.project.careNdShare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Changed from LAZY to EAGER
    @JoinColumn(name = "target_product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"}) // Prevent circular reference
    private Product targetProduct;

    @Column(name = "exchange_item_name", nullable = false)
    private String exchangeItemName;

    @Column(name = "exchange_item_category", nullable = false)
    private String exchangeItemCategory;

    @Column(name = "exchange_item_description", columnDefinition = "TEXT")
    private String exchangeItemDescription;

    @Column(name = "exchange_item_image")
    private String exchangeItemImage;

    @Column(name = "additional_message", columnDefinition = "TEXT")
    private String additionalMessage;

    @ManyToOne(fetch = FetchType.EAGER) // Changed from LAZY to EAGER
    @JoinColumn(name = "requester_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "resetToken", "resetTokenExpiry"}) // Exclude sensitive data
    private User requester;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor
    public ExchangeRequest() {
    }

    // Parameterized constructor
    public ExchangeRequest(Product targetProduct, String exchangeItemName, String exchangeItemCategory,
                           String exchangeItemDescription, String exchangeItemImage, String additionalMessage,
                           User requester, String status) {
        this.targetProduct = targetProduct;
        this.exchangeItemName = exchangeItemName;
        this.exchangeItemCategory = exchangeItemCategory;
        this.exchangeItemDescription = exchangeItemDescription;
        this.exchangeItemImage = exchangeItemImage;
        this.additionalMessage = additionalMessage;
        this.requester = requester;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getTargetProduct() {
        return targetProduct;
    }

    public void setTargetProduct(Product targetProduct) {
        this.targetProduct = targetProduct;
    }

    public String getExchangeItemName() {
        return exchangeItemName;
    }

    public void setExchangeItemName(String exchangeItemName) {
        this.exchangeItemName = exchangeItemName;
    }

    public String getExchangeItemCategory() {
        return exchangeItemCategory;
    }

    public void setExchangeItemCategory(String exchangeItemCategory) {
        this.exchangeItemCategory = exchangeItemCategory;
    }

    public String getExchangeItemDescription() {
        return exchangeItemDescription;
    }

    public void setExchangeItemDescription(String exchangeItemDescription) {
        this.exchangeItemDescription = exchangeItemDescription;
    }

    public String getExchangeItemImage() {
        return exchangeItemImage;
    }

    public void setExchangeItemImage(String exchangeItemImage) {
        this.exchangeItemImage = exchangeItemImage;
    }

    public String getAdditionalMessage() {
        return additionalMessage;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Add this method to fix the error in AdminController
    public Product getRequestedProduct() {
        return this.targetProduct;
    }

    // Optional: Add setter for consistency
    public void setRequestedProduct(Product requestedProduct) {
        this.targetProduct = requestedProduct;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "ExchangeRequest{" +
                "id=" + id +
                ", targetProduct=" + (targetProduct != null ? targetProduct.getId() : "null") +
                ", exchangeItemName='" + exchangeItemName + '\'' +
                ", exchangeItemCategory='" + exchangeItemCategory + '\'' +
                ", exchangeItemDescription='" + exchangeItemDescription + '\'' +
                ", exchangeItemImage='" + exchangeItemImage + '\'' +
                ", additionalMessage='" + additionalMessage + '\'' +
                ", requester=" + (requester != null ? requester.getId() : "null") +
                ", status='" + status + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}