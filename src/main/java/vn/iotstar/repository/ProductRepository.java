package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.user u WHERE " +
            "(:admin = true OR u.id = :userId) AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) ",
            countQuery = "SELECT COUNT(p) FROM Product p JOIN p.user u WHERE " +
                    "(:admin = true OR u.id = :userId) AND " +
                    "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<Product> searchVisible(@Param("keyword") String keyword, @Param("userId") Long userId,
                                @Param("admin") boolean admin, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.user WHERE p.id = :id AND (:admin = true OR p.user.id = :userId)")
    Optional<Product> findVisibleById(@Param("id") Long id, @Param("userId") Long userId,
                                      @Param("admin") boolean admin);

    long countByUser_Id(Long userId);
}
