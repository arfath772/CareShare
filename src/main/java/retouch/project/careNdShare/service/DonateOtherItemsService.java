package retouch.project.careNdShare.service;

import org.springframework.stereotype.Service;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.entity.DonateOtherItems;
import retouch.project.careNdShare.repository.DonateOtherItemsRepository;

import java.util.List;

@Service
public class DonateOtherItemsService {

    private final DonateOtherItemsRepository repository;

    public DonateOtherItemsService(DonateOtherItemsRepository repository) {
        this.repository = repository;
    }

    // ✅ Save Donation
    public DonateOtherItems saveDonation(DonateItemDTO dto) {

        DonateOtherItems item = new DonateOtherItems();
        item.setItemName(dto.getItemName());
        item.setQuantity(dto.getQuantity());
        item.setItemCondition(dto.getItemCondition());
        item.setPickupAddress(dto.getPickupAddress());
        item.setUserId(dto.getUserId());
        item.setImageUrl(dto.getImageUrl());

        return repository.save(item);
    }

    // ✅ Get All Donations
    public List<DonateOtherItems> getAllDonations() {
        return repository.findAll();
    }
}


