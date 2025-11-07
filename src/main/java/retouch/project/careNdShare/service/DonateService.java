package retouch.project.careNdShare.service;

import org.springframework.stereotype.Service;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.dto.DonateRequestDTO;
import retouch.project.careNdShare.entity.*;
import retouch.project.careNdShare.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonateService {

    private final DonateItemRepository donateItemRepository;
    private final DonateRequestRepository donateRequestRepository;

    private final DonateClothesRepository clothesRepo;
    private final DonateBooksRepository booksRepo;
    private final DonateToysRepository toysRepo;
    private final DonateOtherItemsRepository otherRepo;



    public DonateService(
            DonateItemRepository donateItemRepository,
            DonateRequestRepository donateRequestRepository,
            DonateClothesRepository clothesRepo,
            DonateBooksRepository booksRepo,
            DonateToysRepository toysRepo,
            DonateOtherItemsRepository otherRepo
    ) {
        this.donateItemRepository = donateItemRepository;
        this.donateRequestRepository = donateRequestRepository;
        this.clothesRepo = clothesRepo;
        this.booksRepo = booksRepo;
        this.toysRepo = toysRepo;
        this.otherRepo = otherRepo;
    }

    // =========================
    // Generic Donations
    // =========================
    public DonateItem createDonation(DonateItemDTO dto) {
        DonateItem item = new DonateItem(
                dto.getItemName(),
                dto.getQuantity(),
                dto.getItemCondition(),
                dto.getPickupAddress(),
                dto.getUserId()
        );
        return donateItemRepository.save(item);
    }
    public List<DonateItem> getAllDonations() {
        return donateItemRepository.findAll(); // ✅ This must exist
    }

    public DonateRequest createDonateRequest(DonateRequestDTO dto) {
        DonateRequest request = new DonateRequest(
                dto.getDonationType(),
                dto.getDonationId(),
                dto.getReceiverUserId(),
                "PENDING",
                LocalDateTime.now()
        );
        return donateRequestRepository.save(request);

    }


    // =========================
    // Donation Requests (NEW)
    // =========================
    public DonateRequest createDonateRequest(DonateRequest request) {
        request.setStatus("PENDING");
        request.setRequestedDate(LocalDateTime.now());
        return donateRequestRepository.save(request);
    }

    public List<DonateRequest> getAllRequests() {
        return donateRequestRepository.findAll();
    }

    // =========================
    // Category-based Donations
    // =========================

    // Clothes
    public DonateClothes donateClothes(DonateClothes item) {
        return clothesRepo.save(item);
    }

    public List<DonateClothes> getAllClothes() {
        return clothesRepo.findAll();
    }

    // Books
    public DonateBooks donateBooks(DonateBooks item) {
        return booksRepo.save(item);
    }

    public List<DonateBooks> getAllBooks() {
        return booksRepo.findAll();
    }

    // Toys
    public DonateToys donateToys(DonateToys item) {
        return toysRepo.save(item);
    }

    public List<DonateToys> getAllToys() {
        return toysRepo.findAll();
    }

    // Other Items
    public DonateOtherItems donateOtherItems(DonateOtherItems item) {
        return otherRepo.save(item);
    }

    public List<DonateOtherItems> getAllOtherItems() {
        return otherRepo.findAll();
    }
}


