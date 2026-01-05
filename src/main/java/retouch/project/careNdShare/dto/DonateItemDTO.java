package retouch.project.careNdShare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class DonateItemDTO {

    @NotBlank(message = "Item type is required")
    private String itemType;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Item condition is required")
    private String itemCondition;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotNull(message = "At least one image file is required")
    @Size(min = 1, message = "At least one image file is required")
    private List<MultipartFile> imageFiles; // Changed to list for multiple images

    // Getters and Setters
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public List<MultipartFile> getImageFiles() { return imageFiles; }
    public void setImageFiles(List<MultipartFile> imageFiles) { this.imageFiles = imageFiles; }
}