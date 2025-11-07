package retouch.project.careNdShare.dto;

public class DonateItemDTO {

    private String itemName;
    private int quantity;
    private String itemCondition;
    private String pickupAddress;
    private Long userId;
    private String imageUrl;

    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public String getItemCondition() { return itemCondition; }
    public String getPickupAddress() { return pickupAddress; }
    public Long getUserId() { return userId; }
    public String getImageUrl() { return imageUrl; }

    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
