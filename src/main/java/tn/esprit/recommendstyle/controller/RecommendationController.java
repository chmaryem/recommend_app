package tn.esprit.recommendstyle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.recommendstyle.entity.OutfitRecommendationHistory;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.repository.OutfitRecommendationHistoryRepo;
import tn.esprit.recommendstyle.repository.UsersRepo;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class RecommendationController {
    @Autowired
    OutfitRecommendationHistoryRepo historyRepo;
    @Autowired
    UsersRepo  usersRepo;


    @PostMapping("/user/saveRecommendation")
    public ResponseEntity<Void> saveRecommendation(@RequestBody Map<String, Object> body, Principal principal) {
        Users user = usersRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        OutfitRecommendationHistory history = new OutfitRecommendationHistory();
        history.setUser(user);
        history.setEmotion((String) body.get("emotion"));
        history.setConfidence(Double.parseDouble(body.get("confidence").toString()));
        history.setWeather((String) body.get("weather"));
        history.setTemperature(body.get("temperature").toString());
        history.setImagePath((String) body.get("imagePath"));
        history.setRecommendedOutfit((String) body.get("outfit"));
        history.setAccepted((Boolean) body.get("accepted"));
        history.setTimestamp(LocalDateTime.now());

        historyRepo.save(history);

        return ResponseEntity.ok().build();
    }
    @GetMapping("/user/history")
    public ResponseEntity<List<OutfitRecommendationHistory>> getUserHistory(Principal principal) {
        Users user = usersRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OutfitRecommendationHistory> historyList = historyRepo.findByUserOrderByTimestampDesc(user);
        return ResponseEntity.ok(historyList);
    }



}
