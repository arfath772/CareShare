package retouch.project.careNdShare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "donate_others")
public class DonateOtherItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private int quantity;
    private String itemCondition;
    private String pickupAddress;
    private Long userId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    public DonateOtherItems() {}

    public DonateOtherItems(String itemName, int quantity, String itemCondition,
                            String pickupAddress, Long userId, String imageUrl) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.itemCondition = itemCondition;
        this.pickupAddress = pickupAddress;
        this.userId = userId;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}


