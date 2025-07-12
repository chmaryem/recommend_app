package tn.esprit.recommendstyle.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "OutfitFeedback")
@Data
@Getter
@Setter
public class OutfitFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String outfitId;
    private Boolean liked;
    private int rating;

    @Column(length = 500)
    private String comment;

    @ElementCollection
    private List<String> tags;

    private LocalDateTime timestamp;
}
