package tn.esprit.recommendstyle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "OutfitRecommendation")
@Data
@Getter
@Setter
public class OutfitRecommendationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emotion;
    private Double confidence;
    private String weather;
    private String temperature;
    private String imagePath;

    @Lob
    private String recommendedOutfit; // JSON string of recommended items

    private Boolean accepted; // null initially, updated when user likes/dislikes
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Users user;
}
