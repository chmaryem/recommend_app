package tn.esprit.recommendstyle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
@Entity
@Data
@Getter
@Setter
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String style;
    private String otherPreferences;
    private LocalTime notificationTime;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
}
