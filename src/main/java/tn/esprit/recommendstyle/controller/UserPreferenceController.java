package tn.esprit.recommendstyle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.recommendstyle.entity.UserPreference;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.service.UserPreferenceService;

import java.util.List;

@RestController

@RequestMapping("/user/preferences") // plus logique que /public
public class UserPreferenceController {
    @Autowired
    private UserPreferenceService service;

    @PostMapping
    public ResponseEntity<UserPreference> save(@RequestBody UserPreference pref) {
        return ResponseEntity.ok(service.saveOrUpdate(pref));
    }

    @GetMapping
    public ResponseEntity<UserPreference> get() {
        return service.getCurrentUserPreference()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> delete() {
        service.deleteCurrentUserPreference();
        return ResponseEntity.noContent().build();
    }


}
