package retouch.project.careNdShare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.DonateRequest;
import retouch.project.careNdShare.entity.DonateRequestStatus;

import java.util.List;

@Repository
public interface DonateRequestRepository extends JpaRepository<DonateRequest, Long> {

    // Find requests by the user who made them
    List<DonateRequest> findByReceiverId(Long receiverId);

    // Find requests by status
    List<DonateRequest> findByStatus(DonateRequestStatus status);

    // Find all requests for a specific donation item
    List<DonateRequest> findByDonateItemId(Long donationId);

    // Find pending requests for a specific donation item
    List<DonateRequest> findByDonateItemIdAndStatus(Long donationId, DonateRequestStatus status);

    // Count requests by status
    long countByStatus(DonateRequestStatus status);
}
