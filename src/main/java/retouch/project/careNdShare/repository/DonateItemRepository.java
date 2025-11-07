package retouch.project.careNdShare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import retouch.project.careNdShare.entity.DonateItem;

public interface DonateItemRepository extends JpaRepository<DonateItem, Long> {
}

