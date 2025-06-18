package tn.esprit.recommendstyle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.recommendstyle.dto.ReqRes;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.repository.UsersRepo;
import tn.esprit.recommendstyle.service.UserManagementService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserManagementController {
    @Autowired
    private UserManagementService usersManagementService;
    @Autowired
    UsersRepo usersRepo;

    @PostMapping("/auth/register")
    public ResponseEntity<ReqRes> regeister(@RequestBody ReqRes reg){
        return ResponseEntity.ok(usersManagementService.register(reg));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.login(req));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ReqRes> refreshToken(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.refreshToken(req));
    }
    @PostMapping("/auth/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map
            <String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        Optional<Users> optionalUser = usersRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("Utilisateur non trouvé");
        }

        Users user = optionalUser.get();
        if (user.isVerified()) {
            return ResponseEntity.ok("Utilisateur déjà vérifié");
        }

        if (!code.equals(user.getVerificationCode())) {
            return ResponseEntity.status(400).body("Code incorrect");
        }

        if (user.getCodeExpirationTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Code expiré");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpirationTime(null);
        usersRepo.save(user);

        return ResponseEntity.ok("Email vérifié avec succès");
    }


    @GetMapping("/admin/get-all-users")
    public ResponseEntity<ReqRes> getAllUsers(){
        return ResponseEntity.ok(usersManagementService.getAllUsers());

    }

    @GetMapping("/admin/get-users/{userId}")
    public ResponseEntity<ReqRes> getUSerByID(@PathVariable Integer userId){
        return ResponseEntity.ok(usersManagementService.getUsersById(userId));

    }

    @PutMapping("/admin/update/{userId}")
    public ResponseEntity<ReqRes> updateUser(@PathVariable Integer userId, @RequestBody Users reqres){
        return ResponseEntity.ok(usersManagementService.updateUser(userId, reqres));
    }

    @GetMapping("/adminuser/get-profile")
    public ResponseEntity<ReqRes> getMyProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        ReqRes response = usersManagementService.getMyInfo(email);
        return  ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/admin/delete/{userId}")
    public ResponseEntity<ReqRes> deleteUSer(@PathVariable Integer userId){
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }


}
