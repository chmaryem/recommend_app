package tn.esprit.recommendstyle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.recommendstyle.entity.UserPreference;
import tn.esprit.recommendstyle.repository.UserPreferenceRepository;

import java.time.LocalTime;
import java.util.List;

@Service
public class NotificationScheduler {

    @Autowired
    private UserPreferenceRepository repository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 * * * * *") // Toutes les minutes
    public void sendDailyNotifications() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        System.out.println("==> Notification Check at: " + now);

        List<UserPreference> allPreferences = repository.findAll();

        for (UserPreference pref : allPreferences) {
            LocalTime userTime = pref.getNotificationTime();
            String email = pref.getUser() != null ? pref.getUser().getEmail() : null;

            System.out.println("Checking user: " + email + " at " + userTime);

            if (email != null && userTime != null && userTime.equals(now)) {
                System.out.println("=> Sending email to " + email);
                emailService.sendNotification(email, pref.getStyle());
            }
        }
    }
}
