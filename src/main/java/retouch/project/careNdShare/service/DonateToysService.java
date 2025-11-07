package retouch.project.careNdShare.service;

import org.springframework.stereotype.Service;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.entity.DonateToys;
import retouch.project.careNdShare.repository.DonateToysRepository;

import java.util.List;

@Service
public class DonateToysService {

    private final DonateToysRepository repository;

    public DonateToysService(DonateToysRepository repository) {
        this.repository = repository;
    }

    public DonateToys addDonation(DonateItemDTO dto) {
        DonateToys toy = new DonateToys(
                dto.getItemName(),
                dto.getQuantity(),
                dto.getItemCondition(),
                dto.getPickupAddress(),
                dto.getUserId(),
                dto.getImageUrl()
        );

        return repository.save(toy);
    }

    public List<DonateToys> getAllDonations() {
        return repository.findAll();
    }
}



