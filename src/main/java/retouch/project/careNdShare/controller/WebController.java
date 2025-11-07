package retouch.project.careNdShare.controller;

// ... all your imports ...
import org.springframework.web.bind.annotation.RequestParam;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // THIS IS THE CORRECT METHOD FOR /dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = authService.getCurrentUser();
        if (user != null) {
            model.addAttribute("user", user);
            return "dashboard";
        }
        // If no user is authenticated, redirect to login
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "forgot-password";
    }
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        if (token != null) {
            model.addAttribute("token", token);
        }
        return "reset-password";
    }

    @GetMapping("/admin")
    public String adminPanel(Model model) {
        User user = authService.getCurrentUser();
        if (user != null && user.isAdmin()) {
            model.addAttribute("user", user);
            return "admin";
        }
        return "redirect:/dashboard";
    }
}