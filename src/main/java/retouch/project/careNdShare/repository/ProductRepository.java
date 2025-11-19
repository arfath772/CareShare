package retouch.project.careNdShare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.ProductStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByUserIdAndStatus(Long userId, ProductStatus status);
    List<Product> findByStatusAndType(ProductStatus status, String type);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = retouch.project.careNdShare.entity.ProductStatus.PENDING")
    long countPendingProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'APPROVED'")
    long countApprovedProducts();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.user WHERE p.id = :id AND p.status = 'APPROVED'")
    Optional<Product> findByIdWithUser(@Param("id") Long id);

    // ✅ Query for Admin/User Dashboard (Eagerly loads user)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.user WHERE p.status = :status")
    List<Product> findByStatusWithUser(@Param("status") ProductStatus status);

    // ❗ NEW CRITICAL QUERY for fetching MY ITEMS list
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.user WHERE p.user.id = :userId")
    List<Product> findByUserIdWithUser(@Param("userId") Long userId);
}