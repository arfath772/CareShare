package retouch.project.careNdShare.dto;

// PurchaseResponseDTO.java

import java.time.LocalDateTime;

public class PurchaseResponseDto {
    private Long purchaseId;
    private String productName;
    private String sellerName;
    private Double amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String message;

    // Constructors
    public PurchaseResponseDto() {}

    public PurchaseResponseDto(Long purchaseId, String productName, String sellerName,
                               Double amount, String status, String paymentMethod,
                               LocalDateTime createdAt, String message) {
        this.purchaseId = purchaseId;
        this.productName = productName;
        this.sellerName = sellerName;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.message = message;
    }

    // Getters and Setters
    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}