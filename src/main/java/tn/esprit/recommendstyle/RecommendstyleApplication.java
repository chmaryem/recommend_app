package tn.esprit.recommendstyle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RecommendstyleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendstyleApplication.class, args);
    }

}
