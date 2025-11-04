package retouch.project.careNdShare.repository;


import retouch.project.careNdShare.entity.PurchaseRequest;
import retouch.project.careNdShare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseRequest, Long> {
    List<PurchaseRequest> findByBuyerOrderByCreatedAtDesc(User buyer);
    List<PurchaseRequest> findByProductUserOrderByCreatedAtDesc(User seller);
    List<PurchaseRequest> findByProductId(Long productId);
    List<PurchaseRequest> findByStatusOrderByCreatedAtDesc(retouch.project.careNdShare.entity.PurchaseStatus status);
}
