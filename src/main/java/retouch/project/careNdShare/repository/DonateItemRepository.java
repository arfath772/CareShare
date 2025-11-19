package retouch.project.careNdShare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.DonateItem;
import retouch.project.careNdShare.entity.DonationStatus; // Ensure this import is correct

import java.util.List;

@Repository
public interface DonateItemRepository extends JpaRepository<DonateItem, Long> {

    // Find items by a specific status
    List<DonateItem> findByStatus(DonationStatus status);

    // Find items by user ID
    List<DonateItem> findByUserId(Long userId);

    // Find items by user ID and status
    List<DonateItem> findByUserIdAndStatus(Long userId, DonationStatus status);

    // Find by item type and approved status
    List<DonateItem> findByItemTypeIgnoreCaseAndStatus(String itemType, DonationStatus status);

    // ✅ ADD THIS METHOD FOR EFFICIENT STATS
    long countByStatus(DonationStatus status);
}