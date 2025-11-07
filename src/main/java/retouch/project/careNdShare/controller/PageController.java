package retouch.project.careNdShare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/donate")
    public String donatePage() {
        return "donate";
    }

    /*
     *
     * DELETE THIS METHOD TO FIX THE ERROR
     *
     * @GetMapping("/dashboard")
     * public String dashboardPage() {
     * return "dashboard";
     * }
     *
     */

    @GetMapping("/donate-now")
    public String donateNow() {
        return "donate-now"; // This loads donate-now.html from templates folder
    }

    @GetMapping("/request-now")
    public String requestNow() {
        return "request-now";
    }

    @GetMapping("/donate-items")
    public String donateItemsPage() {
        return "donate-items"; // matches donate-items.html
    }

    @GetMapping("/why-donation")
    public String whyDonation() {
        return "why-donation"; // must match why-donation.html
    }
}