package tn.esprit.recommendstyle.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.recommendstyle.entity.Users;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("update Users u set u.password= ?2 where u.email = ?1")
    void updatePassword(String email, String password);


}
