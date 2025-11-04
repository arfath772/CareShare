package retouch.project.careNdShare.dto;

// PurchaseRequestDTO.java

import jakarta.validation.constraints.*;

public class PurchaseRequestDto {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Valid 10-digit phone number is required")
    private String phone;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // UPI, CARD, NETBANKING, COD

    // Constructors
    public PurchaseRequestDto() {}

    public PurchaseRequestDto(Long productId, String fullName, String email, String phone,
                              String shippingAddress, String paymentMethod) {
        this.productId = productId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

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
}
