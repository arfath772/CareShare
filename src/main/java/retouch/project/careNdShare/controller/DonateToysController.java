package retouch.project.careNdShare.controller;

import org.springframework.web.bind.annotation.*;
import retouch.project.careNdShare.dto.DonateItemDTO;
import retouch.project.careNdShare.entity.DonateToys;
import retouch.project.careNdShare.service.DonateToysService;

import java.util.List;

@RestController
@RequestMapping("/api/toys")
@CrossOrigin(origins = "*")
public class DonateToysController {

    private final DonateToysService service;

    public DonateToysController(DonateToysService service) {
        this.service = service;
    }

    @PostMapping
    public DonateToys addDonation(@RequestBody DonateItemDTO dto) {
        return service.addDonation(dto);
    }

    @GetMapping("/all")
    public List<DonateToys> getAll() {
        return service.getAllDonations();
    }
}
