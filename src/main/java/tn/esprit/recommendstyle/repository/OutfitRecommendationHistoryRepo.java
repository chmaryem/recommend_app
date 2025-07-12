package tn.esprit.recommendstyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.recommendstyle.entity.OutfitRecommendationHistory;
import tn.esprit.recommendstyle.entity.Users;

import java.util.List;

public interface OutfitRecommendationHistoryRepo extends JpaRepository<OutfitRecommendationHistory, Long> {
    List<OutfitRecommendationHistory> findByUserOrderByTimestampDesc(Users user);
}