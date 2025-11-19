package retouch.project.careNdShare.dto;

import lombok.Data;
import retouch.project.careNdShare.entity.DonateRequest;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.entity.DonateRequestStatus;

import java.time.LocalDateTime;

@Data
public class DonateRequestResponseDTO {
    private Long id;
    private DonateItemResponseDTO donateItem;
    private UserInfo receiver;
    private DonateRequestStatus status;
    private LocalDateTime requestedDate;
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
        this.rejectionReason = request.getRejectionReason();

        if (request.getDonateItem() != null) {
            this.donateItem = new DonateItemResponseDTO(request.getDonateItem());
        }
        if (request.getReceiver() != null) {
            this.receiver = new UserInfo(request.getReceiver());
        }
    }
}
