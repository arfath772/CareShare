package retouch.project.careNdShare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.DonateRequest;

@Repository
public interface DonateRequestRepository extends JpaRepository<DonateRequest, Long> {
    // no extra methods needed for now
}

