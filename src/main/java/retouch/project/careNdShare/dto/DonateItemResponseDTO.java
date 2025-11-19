package retouch.project.careNdShare.dto;

import lombok.Data;
import retouch.project.careNdShare.entity.DonateItem;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.DonationStatus;

import java.time.LocalDateTime;

@Data
public class DonateItemResponseDTO {
    private Long id;
    private String itemType;
    private String itemName;
    private int quantity;
    private String itemCondition;
    private String pickupAddress;
    private String imageUrl;
    private DonationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private UserInfo donor;

    @Data
    private static class UserInfo {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;

        public UserInfo(User user) {
            this.userId = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
        }
    }

    public DonateItemResponseDTO(DonateItem item) {
        this.id = item.getId();
        this.itemType = item.getItemType();
        this.itemName = item.getItemName();
        this.quantity = item.getQuantity();
        this.itemCondition = item.getItemCondition();
        this.pickupAddress = item.getPickupAddress();
        this.imageUrl = item.getImageUrl();
        this.status = item.getStatus();
        this.rejectionReason = item.getRejectionReason();
        this.createdAt = item.getCreatedAt();
        if (item.getUser() != null) {
            this.donor = new UserInfo(item.getUser());
        }
    }
}