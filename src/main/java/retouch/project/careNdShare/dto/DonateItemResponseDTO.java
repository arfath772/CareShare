package retouch.project.careNdShare.dto;

import lombok.Data;
import retouch.project.careNdShare.entity.DonateItem;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.DonationStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
public class DonateItemResponseDTO {
    private Long id;
    private String itemType;
    private String itemName;
    private int quantity;
    private String itemCondition;
    private String pickupAddress;
    private String mainImageUrl;
    private List<String> imageUrls; // List of all image URLs
    private DonationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserInfo donor;

    @Data
    private static class UserInfo {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone; // Add phone if available

        public UserInfo(User user) {
            this.userId = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            // Add phone if your User entity has it
            // this.phone = user.getPhone();
        }
    }

    public DonateItemResponseDTO(DonateItem item) {
        this.id = item.getId();
        this.itemType = item.getItemType();
        this.itemName = item.getItemName();
        this.quantity = item.getQuantity();
        this.itemCondition = item.getItemCondition();
        this.pickupAddress = item.getPickupAddress();
        this.mainImageUrl = item.getMainImageUrl();
        this.status = item.getStatus();
        this.rejectionReason = item.getRejectionReason();
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();

        // Parse JSON array of image URLs
        try {
            if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                this.imageUrls = mapper.readValue(item.getImageUrls(), new TypeReference<List<String>>() {});
            } else {
                this.imageUrls = new ArrayList<>();
                if (item.getMainImageUrl() != null) {
                    this.imageUrls.add(item.getMainImageUrl());
                }
            }
        } catch (Exception e) {
            this.imageUrls = new ArrayList<>();
            if (item.getMainImageUrl() != null) {
                this.imageUrls.add(item.getMainImageUrl());
            }
        }

        if (item.getUser() != null) {
            this.donor = new UserInfo(item.getUser());
        }
    }
}