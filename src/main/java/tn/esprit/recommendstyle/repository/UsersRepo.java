package tn.esprit.recommendstyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.recommendstyle.entity.Users;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);


}
