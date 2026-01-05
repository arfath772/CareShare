package retouch.project.careNdShare.dto;

import lombok.Data;
import retouch.project.careNdShare.entity.DonateRequest;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.DonateRequestStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class DonateRequestResponseDTO {
    private Long id;
    private DonateItemResponseDTO donateItem;
    private UserInfo receiver;
    private DonateRequestStatus status;
    private LocalDateTime requestedDate;
    private String formattedRequestDate; // Add formatted date
    private String rejectionReason;

    @Data
    private static class UserInfo {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;

        public UserInfo(User user) {
            this.userId = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
        }
    }

    public DonateRequestResponseDTO(DonateRequest request) {
        this.id = request.getId();
        this.status = request.getStatus();
        this.requestedDate = request.getRequestedDate();

        // Format the date for display
        if (request.getRequestedDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            this.formattedRequestDate = request.getRequestedDate().format(formatter);
        }

        this.rejectionReason = request.getRejectionReason();

        if (request.getDonateItem() != null) {
            this.donateItem = new DonateItemResponseDTO(request.getDonateItem());
        }
        if (request.getReceiver() != null) {
            this.receiver = new UserInfo(request.getReceiver());
        }
    }
}