package retouch.project.careNdShare.service;

import retouch.project.careNdShare.dto.PurchaseRequestDto;
import retouch.project.careNdShare.dto.PurchaseResponseDto;
import retouch.project.careNdShare.entity.PurchaseRequest;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.PurchaseStatus;
import retouch.project.careNdShare.entity.ProductStatus;

import retouch.project.careNdShare.repository.PurchaseRepository;
import retouch.project.careNdShare.repository.ProductRepository;
import retouch.project.careNdShare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public PurchaseResponseDto createPurchase(PurchaseRequestDto purchaseRequestDTO, String buyerEmail) {
        try {
            // Find product
            Optional<Product> productOpt = productRepository.findById(purchaseRequestDTO.getProductId());
            if (productOpt.isEmpty()) {
                throw new RuntimeException("Product not found");
            }

            Product product = productOpt.get();

            // Check if product is available (only APPROVED products can be purchased)
            if (product.getStatus() != ProductStatus.APPROVED) {
                throw new RuntimeException("Product is not available for purchase. Current status: " + product.getStatus());
            }

            // Find buyer
            Optional<User> buyerOpt = userRepository.findByEmail(buyerEmail);
            if (buyerOpt.isEmpty()) {
                throw new RuntimeException("Buyer not found");
            }

            User buyer = buyerOpt.get();

            // Check if user is trying to buy their own product
            if (product.getUser().getId().equals(buyer.getId())) {
                throw new RuntimeException("You cannot purchase your own product");
            }

            // Create purchase request
            PurchaseRequest purchaseRequest = new PurchaseRequest(
                    product,
                    buyer,
                    purchaseRequestDTO.getFullName(),
                    purchaseRequestDTO.getEmail(),
                    purchaseRequestDTO.getPhone(),
                    purchaseRequestDTO.getShippingAddress(),
                    purchaseRequestDTO.getPaymentMethod(),
                    product.getPrice()
            );

            // Save purchase request
            PurchaseRequest savedPurchase = purchaseRepository.save(purchaseRequest);

            // MARK PRODUCT AS SOLD IMMEDIATELY - regardless of payment method
            product.setStatus(ProductStatus.SOLD);
            productRepository.save(product);

            // Send notification to seller (non-blocking)
            sendNotificationToSeller(product.getUser(), savedPurchase);

            // Send email notifications ASYNCHRONOUSLY (non-blocking)
            CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendPurchaseNotifications(savedPurchase);
                } catch (Exception e) {
                    System.err.println("Failed to send email notifications: " + e.getMessage());
                }
            });

            // Prepare response - return immediately without waiting for emails
            return new PurchaseResponseDto(
                    savedPurchase.getId(),
                    product.getName(),
                    product.getUser().getFirstName() + " " + product.getUser().getLastName(),
                    product.getPrice(),
                    savedPurchase.getStatus().name(),
                    savedPurchase.getPaymentMethod(),
                    savedPurchase.getCreatedAt(),
                    "Purchase completed successfully! " +
                            ("COD".equals(purchaseRequestDTO.getPaymentMethod()) ?
                                    "Please keep cash ready for delivery." :
                                    "Your payment has been processed successfully.")
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create purchase: " + e.getMessage());
        }
    }

    public List<PurchaseRequest> getPurchasesByBuyer(String buyerEmail) {
        Optional<User> buyerOpt = userRepository.findByEmail(buyerEmail);
        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found");
        }
        return purchaseRepository.findByBuyerOrderByCreatedAtDesc(buyerOpt.get());
    }

    public List<PurchaseRequest> getSalesBySeller(String sellerEmail) {
        Optional<User> sellerOpt = userRepository.findByEmail(sellerEmail);
        if (sellerOpt.isEmpty()) {
            throw new RuntimeException("Seller not found");
        }
        return purchaseRepository.findByProductUserOrderByCreatedAtDesc(sellerOpt.get());
    }

    @Transactional
    public PurchaseRequest updatePurchaseStatus(Long purchaseId, PurchaseStatus status, String userEmail) {
        Optional<PurchaseRequest> purchaseOpt = purchaseRepository.findById(purchaseId);
        if (purchaseOpt.isEmpty()) {
            throw new RuntimeException("Purchase not found");
        }

        PurchaseRequest purchase = purchaseOpt.get();

        // Check if user is the seller
        if (!purchase.getProduct().getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to update this purchase");
        }

        purchase.setStatus(status);

        // Send status update email ASYNCHRONOUSLY
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendStatusUpdateEmail(purchase);
            } catch (Exception e) {
                System.err.println("Failed to send status update email: " + e.getMessage());
            }
        });

        return purchaseRepository.save(purchase);
    }

    private void sendNotificationToSeller(User seller, PurchaseRequest purchase) {
        // Implement notification logic here (email, push notification, etc.)
        System.out.println("Notification: New purchase request for product '" +
                purchase.getProduct().getName() + "' from " +
                purchase.getFullName() + " (" + purchase.getEmail() + ")");
        System.out.println("Payment Method: " + purchase.getPaymentMethod());
        System.out.println("Amount: ₹" + purchase.getAmount());
    }
}