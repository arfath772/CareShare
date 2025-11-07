package retouch.project.careNdShare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "donate_items")
public class DonateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    private String itemName;
    private int quantity;
    private String itemCondition;
    private String pickupAddress;

    private Long userId; // ✅ Added to match DB

    public DonateItem() {}

    public DonateItem(String itemName, int quantity, String itemCondition, String pickupAddress, Long userId) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.itemCondition = itemCondition;
        this.pickupAddress = pickupAddress;
        this.userId = userId;
    }

    public Long getDonationId() { return donationId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public String getItemCondition() { return itemCondition; }
    public String getPickupAddress() { return pickupAddress; }
    public Long getUserId() { return userId; }

    public void setDonationId(Long donationId) { this.donationId = donationId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public void setUserId(Long userId) { this.userId = userId; }
}


