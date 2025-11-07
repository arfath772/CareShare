package retouch.project.careNdShare.service;

import org.springframework.stereotype.Service;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.entity.DonateBooks;
import retouch.project.careNdShare.repository.DonateBooksRepository;

import java.util.List;

@Service
public class DonateBooksService {

    private final DonateBooksRepository repository;

    public DonateBooksService(DonateBooksRepository repository) {
        this.repository = repository;
    }

    public DonateBooks saveDonation(DonateItemDTO dto) {
        DonateBooks book = new DonateBooks(
                dto.getItemName(),
                dto.getQuantity(),
                dto.getItemCondition(),
                dto.getPickupAddress(),
                dto.getUserId(),
                dto.getImageUrl()
        );
        return repository.save(book);
    }


    public List<DonateBooks> getAll() {
        return repository.findAll();
    }
}



