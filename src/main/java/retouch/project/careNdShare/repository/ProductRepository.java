package retouch.project.careNdShare.repository;

import org.springframework.data.repository.query.Param;
import retouch.project.careNdShare.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.ProductStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByUserIdAndStatus(Long userId, ProductStatus status);
    List<Product> findByStatusAndType(ProductStatus status, String type);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'PENDING'")
    long countPendingProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'APPROVED'")
    long countApprovedProducts();

    // Add this method for detailed product view
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.user WHERE p.id = :id AND p.status = 'APPROVED'")
    Optional<Product> findByIdWithUser(Long id);

    // In your repository or service
    @Query(value = "INSERT INTO products (..., product_type, ...) VALUES (..., :productType, ...)", nativeQuery = true)
    void saveProduct(@Param("productType") String productType);
}