package tn.esprit.recommendstyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.recommendstyle.entity.UserPreference;
import tn.esprit.recommendstyle.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    List<UserPreference> findByUser(Users user);

}
