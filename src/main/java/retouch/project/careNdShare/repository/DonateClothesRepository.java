package retouch.project.careNdShare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.DonateClothes;

@Repository
public interface DonateClothesRepository extends JpaRepository<DonateClothes, Long> {
}

