package retouch.project.careNdShare.service;

import org.springframework.stereotype.Service;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.entity.DonateClothes;
import retouch.project.careNdShare.repository.DonateClothesRepository;

import java.util.List;

@Service
public class DonateClothesService {

    private final DonateClothesRepository repository;

    public DonateClothesService(DonateClothesRepository repository) {
        this.repository = repository;
    }

    public DonateClothes addDonation(DonateItemDTO dto) {

        DonateClothes clothes = new DonateClothes(
                dto.getItemName(),
                dto.getQuantity(),
                dto.getItemCondition(),
                dto.getPickupAddress(),
                dto.getUserId(),
                dto.getImageUrl()   // ✅ Save image
        );

        return repository.save(clothes);
    }

    public List<DonateClothes> getAllDonations() {
        return repository.findAll();
    }
}

