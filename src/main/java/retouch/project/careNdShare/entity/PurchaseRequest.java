package retouch.project.careNdShare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
public class PurchaseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User buyer;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private String paymentMethod; // UPI, CARD, NETBANKING, COD

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public PurchaseRequest() {
        this.createdAt = LocalDateTime.now();
    }

    public PurchaseRequest(Product product, User buyer, String fullName, String email,
                           String phone, String shippingAddress, String paymentMethod, Double amount) {
        this();
        this.product = product;
        this.buyer = buyer;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public PurchaseStatus getStatus() { return status; }
    public void setStatus(PurchaseStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // New method to get first name from full name
    public String getFirstName() {
        if (this.fullName != null && !this.fullName.trim().isEmpty()) {
            // Split by space and return the first part
            return this.fullName.split(" ")[0];
        }
        return this.fullName;
    }

    // Optional: Add method to get last name if needed
    public String getLastName() {
        if (this.fullName != null && !this.fullName.trim().isEmpty()) {
            String[] names = this.fullName.split(" ");
            if (names.length > 1) {
                // Return the last part as last name
                return names[names.length - 1];
            }
        }
        return "";
    }
}