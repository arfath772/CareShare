package retouch.project.careNdShare.repository;

import retouch.project.careNdShare.entity.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findByRequesterId(Long requesterId);
    List<ExchangeRequest> findByRequesterIdAndStatus(Long requesterId, String status);
    List<ExchangeRequest> findByStatus(String status);
    long countByStatus(String status);

    // Find exchange requests by product owner
    List<ExchangeRequest> findByTargetProductUserId(Long ownerId);
    List<ExchangeRequest> findByTargetProductUserIdAndStatus(Long ownerId, String status);

    // Find by product ID
    List<ExchangeRequest> findByTargetProductId(Long productId);
    List<ExchangeRequest> findByTargetProductIdAndStatus(Long productId, String status);

    // Custom query to fetch exchange requests with eager loading of relationships
    @Query("SELECT er FROM ExchangeRequest er " +
            "LEFT JOIN FETCH er.requester " +
            "LEFT JOIN FETCH er.targetProduct tp " +
            "LEFT JOIN FETCH tp.user " +
            "WHERE (:status IS NULL OR er.status = :status) " +
            "ORDER BY er.createdAt DESC")
    List<ExchangeRequest> findAllWithUsersAndProducts(@Param("status") String status);

    // Custom query for user-specific requests with eager loading
    @Query("SELECT er FROM ExchangeRequest er " +
            "LEFT JOIN FETCH er.requester " +
            "LEFT JOIN FETCH er.targetProduct tp " +
            "LEFT JOIN FETCH tp.user " +
            "WHERE er.requester.id = :requesterId AND (:status IS NULL OR er.status = :status) " +
            "ORDER BY er.createdAt DESC")
    List<ExchangeRequest> findByRequesterIdWithDetails(@Param("requesterId") Long requesterId, @Param("status") String status);

    // Custom query for product owner requests with eager loading
    @Query("SELECT er FROM ExchangeRequest er " +
            "LEFT JOIN FETCH er.requester " +
            "LEFT JOIN FETCH er.targetProduct tp " +
            "LEFT JOIN FETCH tp.user " +
            "WHERE tp.user.id = :ownerId AND (:status IS NULL OR er.status = :status) " +
            "ORDER BY er.createdAt DESC")
    List<ExchangeRequest> findByTargetProductUserIdWithDetails(@Param("ownerId") Long ownerId, @Param("status") String status);

    // Check if user already has a pending request for a product
    @Query("SELECT COUNT(er) > 0 FROM ExchangeRequest er WHERE er.requester.id = :userId AND er.targetProduct.id = :productId AND er.status = 'PENDING'")
    boolean existsPendingRequestForProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}