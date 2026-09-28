package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.username) = LOWER(:login) OR LOWER(u.email) = LOWER(:login)")
    Optional<User> findByUsernameOrEmailWithRole(@Param("login") String login);

    @Query(value = "SELECT u FROM User u JOIN FETCH u.role WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(u) FROM User u WHERE " +
                    "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                    "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId AND u.enabled = true")
    long countEnabledUsersByRoleId(@Param("roleId") Long roleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT u.id AS userId, COUNT(p.id) AS productCount FROM User u LEFT JOIN u.products p " +
            "WHERE u.id IN :ids GROUP BY u.id")
    java.util.List<UserProductCount> countProductsForUsers(@Param("ids") java.util.Collection<Long> ids);

    interface UserProductCount {
        Long getUserId();
        Long getProductCount();
    }
}
