package tn.esprit.recommendstyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.recommendstyle.entity.ForgotPassword;
import tn.esprit.recommendstyle.entity.Users;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {
    @Query("select fp from ForgotPassword fp where fp.otp = ?1 and fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, Users user);
    @Modifying
    @Query("DELETE FROM ForgotPassword fp WHERE fp.user = :user")
    void deleteByUser(@Param("user") Users user);



}
