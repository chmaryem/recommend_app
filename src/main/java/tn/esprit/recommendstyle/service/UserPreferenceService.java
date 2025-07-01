package tn.esprit.recommendstyle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tn.esprit.recommendstyle.entity.UserPreference;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.repository.UserPreferenceRepository;
import tn.esprit.recommendstyle.repository.UsersRepo;


import java.util.List;
import java.util.Optional;
@Service
public class UserPreferenceService {
    @Autowired
    private UserPreferenceRepository repository;

    @Autowired
    private UsersRepo usersRepository;

    public UserPreference saveOrUpdate(UserPreference pref) {
        // Récupérer l'utilisateur connecté via Spring Security
        String email = getCurrentUserEmail();

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        List<UserPreference> existingPrefs = repository.findByUser(user);

        if (!existingPrefs.isEmpty()) {
            UserPreference existingPref = existingPrefs.get(0);
            existingPref.setStyle(pref.getStyle());
            existingPref.setOtherPreferences(pref.getOtherPreferences());
            existingPref.setNotificationTime(pref.getNotificationTime());
            return repository.save(existingPref);
        } else {
            pref.setUser(user);
            return repository.save(pref);
        }
    }

    public Optional<UserPreference> getCurrentUserPreference() {
        String email = getCurrentUserEmail();
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
        return repository.findByUser(user).stream().findFirst();
    }

    public void deleteCurrentUserPreference() {
        String email = getCurrentUserEmail();
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        repository.findByUser(user).forEach(repository::delete);
    }

    public List<UserPreference> getAll() {
        return repository.findAll();
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }
}
