package retouch.project.careNdShare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.dto.DonateItemResponseDTO;
import retouch.project.careNdShare.entity.DonateItem;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.DonationStatus;
import retouch.project.careNdShare.repository.DonateItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DonateItemService {

    @Autowired
    private DonateItemRepository donateItemRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String UPLOAD_DIR = "uploads/donations/";

    // ✅ Add a new donation (sets status to PENDING)
    public DonateItemResponseDTO addDonation(DonateItemDTO dto) throws IOException {
        User currentUser = authService.getCurrentUser();

        // Save all images
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : dto.getImageFiles()) {
            if (!file.isEmpty()) {
                String imageUrl = saveImage(file, currentUser.getId());
                imageUrls.add(imageUrl);
            }
        }

        if (imageUrls.isEmpty()) {
            throw new RuntimeException("At least one image is required");
        }

        DonateItem item = new DonateItem();
        item.setItemType(dto.getItemType());
        item.setItemName(dto.getItemName());
        item.setQuantity(dto.getQuantity());
        item.setItemCondition(dto.getItemCondition());
        item.setPickupAddress(dto.getPickupAddress());
        item.setUser(currentUser);
        item.setStatus(DonationStatus.PENDING); // Default status

        // Set main image as first image
        if (!imageUrls.isEmpty()) {
            item.setMainImageUrl(imageUrls.get(0));
        }

        // Store all image URLs as JSON array
        try {
            String imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
            item.setImageUrls(imageUrlsJson);
        } catch (Exception e) {
            throw new RuntimeException("Error processing images");
        }

        DonateItem savedItem = donateItemRepository.save(item);
        return new DonateItemResponseDTO(savedItem);
    }

    // ✅ Helper to save image (Corrected Path)
    private String saveImage(MultipartFile file, Long userId) throws IOException {
        String folder = UPLOAD_DIR + userId + "/";
        Files.createDirectories(Paths.get(folder));

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        String fileName = UUID.randomUUID().toString() + extension;

        Path filePath = Paths.get(folder + fileName);
        Files.write(filePath, file.getBytes());

        return folder + fileName;
    }

    // ✅ Get all donations (for admin)
    public List<DonateItemResponseDTO> getAllDonations() {
        return donateItemRepository.findAll().stream()
                .map(DonateItemResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get all PENDING donations (for admin approval)
    public List<DonateItemResponseDTO> getPendingDonations() {
        return donateItemRepository.findByStatus(DonationStatus.PENDING).stream()
                .map(DonateItemResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get all APPROVED donations (for public listing)
    public List<DonateItemResponseDTO> getAvailableDonations(String type) {
        List<DonateItem> items;
        if (type == null || type.isBlank() || type.equalsIgnoreCase("all")) {
            items = donateItemRepository.findByStatus(DonationStatus.APPROVED);
        } else {
            items = donateItemRepository.findByItemTypeIgnoreCaseAndStatus(type, DonationStatus.APPROVED);
        }
        return items.stream()
                .map(DonateItemResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get donations by the currently logged-in user
    public List<DonateItemResponseDTO> getMyDonations() {
        User currentUser = authService.getCurrentUser();
        return donateItemRepository.findByUserId(currentUser.getId()).stream()
                .map(DonateItemResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get a single donation by ID
    public DonateItemResponseDTO getDonationById(Long id) {
        DonateItem item = donateItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + id));
        return new DonateItemResponseDTO(item);
    }

    // ✅ Approve a donation (for admin)
    public DonateItemResponseDTO approveDonation(Long id) {
        DonateItem item = donateItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation item not found with ID: " + id));

        item.setStatus(DonationStatus.APPROVED);
        item.setRejectionReason(null);
        DonateItem savedItem = donateItemRepository.save(item);
        return new DonateItemResponseDTO(savedItem);
    }

    // ✅ Reject a donation (for admin)
    public DonateItemResponseDTO rejectDonation(Long id, String reason) {
        DonateItem item = donateItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation item not found with ID: " + id));

        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Rejection reason is required");
        }

        item.setStatus(DonationStatus.REJECTED);
        item.setRejectionReason(reason);
        DonateItem savedItem = donateItemRepository.save(item);
        return new DonateItemResponseDTO(savedItem);
    }

    // ✅ Mark a donation as CLAIMED (called by DonateRequestService)
    public void claimDonation(Long id) {
        DonateItem item = donateItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation item not found with ID: " + id));

        if (item.getStatus() != DonationStatus.APPROVED) {
            throw new RuntimeException("Donation item is not approved and cannot be claimed.");
        }

        item.setStatus(DonationStatus.CLAIMED);
        donateItemRepository.save(item);
    }

    // ✅ Delete a donation
    public void deleteDonation(Long id) {
        User currentUser = authService.getCurrentUser();
        DonateItem item = donateItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + id));

        // Only admin or the original donor can delete
        if (!item.getUser().getId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new RuntimeException("You are not authorized to delete this item.");
        }

        // TODO: Delete image files from storage

        donateItemRepository.delete(item);
    }
}