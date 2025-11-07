package retouch.project.careNdShare.controller;

import org.springframework.web.bind.annotation.*;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.entity.DonateOtherItems;
import retouch.project.careNdShare.service.DonateOtherItemsService;

import java.util.List;

@RestController
@RequestMapping("/api/donate/other")
@CrossOrigin(origins = "*")
public class DonateOtherItemsController {

    private final DonateOtherItemsService service;

    public DonateOtherItemsController(DonateOtherItemsService service) {
        this.service = service;
    }

    // ✅ Add Donation
    @PostMapping("/add")
    public DonateOtherItems addDonation(@RequestBody DonateItemDTO dto) {
        return service.saveDonation(dto);
    }

    // ✅ Get All Donations
    @GetMapping("/all")
    public List<DonateOtherItems> getAll() {
        return service.getAllDonations();
    }
}


