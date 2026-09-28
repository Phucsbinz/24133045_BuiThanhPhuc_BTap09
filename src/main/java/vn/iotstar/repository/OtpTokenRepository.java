package vn.iotstar.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.OtpToken;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(String email, String otpType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM OtpToken t WHERE LOWER(t.email) = LOWER(:email) AND t.otpType = :otpType " +
            "AND t.used = false ORDER BY t.createdAt DESC")
    java.util.List<OtpToken> findUnusedForUpdate(@Param("email") String email, @Param("otpType") String otpType,
                                                 org.springframework.data.domain.Pageable pageable);

    void deleteByEmailIgnoreCaseAndOtpType(String email, String otpType);
    void deleteByEmailIgnoreCase(String email);
}
