package retouch.project.careNdShare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // Never send password in API responses
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    @Column(name = "reset_token")
    @JsonIgnore
    private String resetToken;

    @Column(name = "reset_token_expiry")
    @JsonIgnore
    private LocalDateTime resetTokenExpiry;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    // --- NEW RELATIONSHIPS ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DonateItem> donatedItems;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DonateRequest> receivedRequests;

    // --- Existing Constructors, Getters, and Setters ---

    public User() {}

    public User(String email, String password, String firstName, String lastName, List<String> roles) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    // Getters and Setters for all fields...
    // (id, email, password, firstName, lastName, roles, resetToken, resetTokenExpiry, isAdmin)
    // ...

    // --- ADD GETTERS/SETTERS FOR NEW FIELDS ---
    public List<DonateItem> getDonatedItems() {
        return donatedItems;
    }

    public void setDonatedItems(List<DonateItem> donatedItems) {
        this.donatedItems = donatedItems;
    }

    public List<DonateRequest> getReceivedRequests() {
        return receivedRequests;
    }

    public void setReceivedRequests(List<DonateRequest> receivedRequests) {
        this.receivedRequests = receivedRequests;
    }

    // (Keep all your other existing getters and setters)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}