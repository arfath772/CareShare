package retouch.project.careNdShare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Product targetProduct;

    @Column(name = "exchange_item_name", nullable = false)
    private String exchangeItemName;

    @Column(name = "exchange_item_category", nullable = false)
    private String exchangeItemCategory;

    @Column(name = "exchange_item_description", columnDefinition = "TEXT")
    private String exchangeItemDescription;

    @ElementCollection
    @CollectionTable(name = "exchange_request_images",
            joinColumns = @JoinColumn(name = "exchange_request_id"))
    @Column(name = "image_url")
    private List<String> exchangeItemImages = new ArrayList<>();

    @Column(name = "additional_message", columnDefinition = "TEXT")
    private String additionalMessage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "resetToken", "resetTokenExpiry"})
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
                           String exchangeItemDescription, List<String> exchangeItemImages, String additionalMessage,
                           User requester, String status) {
        this.targetProduct = targetProduct;
        this.exchangeItemName = exchangeItemName;
        this.exchangeItemCategory = exchangeItemCategory;
        this.exchangeItemDescription = exchangeItemDescription;
        this.exchangeItemImages = exchangeItemImages;
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

    public List<String> getExchangeItemImages() {
        return exchangeItemImages;
    }

    public void setExchangeItemImages(List<String> exchangeItemImages) {
        this.exchangeItemImages = exchangeItemImages;
    }

    // Helper method to add single image
    public void addExchangeItemImage(String imageUrl) {
        if (this.exchangeItemImages == null) {
            this.exchangeItemImages = new ArrayList<>();
        }
        this.exchangeItemImages.add(imageUrl);
    }

    // Backward compatibility method
    public String getExchangeItemImage() {
        return !exchangeItemImages.isEmpty() ? exchangeItemImages.get(0) : null;
    }

    public void setExchangeItemImage(String exchangeItemImage) {
        if (this.exchangeItemImages == null) {
            this.exchangeItemImages = new ArrayList<>();
        }
        if (!this.exchangeItemImages.isEmpty()) {
            this.exchangeItemImages.set(0, exchangeItemImage);
        } else {
            this.exchangeItemImages.add(exchangeItemImage);
        }
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

    public Product getRequestedProduct() {
        return this.targetProduct;
    }

    public void setRequestedProduct(Product requestedProduct) {
        this.targetProduct = requestedProduct;
    }

    @Override
    public String toString() {
        return "ExchangeRequest{" +
                "id=" + id +
                ", targetProduct=" + (targetProduct != null ? targetProduct.getId() : "null") +
                ", exchangeItemName='" + exchangeItemName + '\'' +
                ", exchangeItemCategory='" + exchangeItemCategory + '\'' +
                ", exchangeItemDescription='" + exchangeItemDescription + '\'' +
                ", exchangeItemImages=" + exchangeItemImages +
                ", additionalMessage='" + additionalMessage + '\'' +
                ", requester=" + (requester != null ? requester.getId() : "null") +
                ", status='" + status + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}