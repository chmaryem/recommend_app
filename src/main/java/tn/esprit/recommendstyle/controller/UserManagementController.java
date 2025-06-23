package tn.esprit.recommendstyle.controller;

import com.twilio.base.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.recommendstyle.dto.ReqRes;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.repository.UsersRepo;
import tn.esprit.recommendstyle.service.FileStorageService;
import tn.esprit.recommendstyle.service.UserManagementService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@RestController
public class UserManagementController {

    private final FileStorageService fileStorageService;
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
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        Optional<Users> optionalUser = usersRepo.findByEmail(email);
        Map<String, String> response = new HashMap<>();

        if (optionalUser.isEmpty()) {
            response.put("message", "Utilisateur non trouvé");
            return ResponseEntity.status(404).body(response);
        }

        Users user = optionalUser.get();
        if (user.isVerified()) {
            response.put("message", "Utilisateur déjà vérifié");
            return ResponseEntity.ok(response);
        }

        if (!code.equals(user.getVerificationCode())) {
            response.put("message", "Code incorrect");
            return ResponseEntity.status(400).body(response);
        }

        if (user.getCodeExpirationTime().isBefore(LocalDateTime.now())) {
            response.put("message", "Code expiré");
            return ResponseEntity.status(400).body(response);
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpirationTime(null);
        usersRepo.save(user);

        response.put("message", "Email vérifié avec succès");
        return ResponseEntity.ok(response);
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




    @PostMapping("/user/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
                                              Principal principal) throws IOException {
        Users user = usersRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = fileStorageService.storeFile(file);
        user.setImage(fileName);
        usersRepo.save(user);

        return ResponseEntity.ok(fileName);
    }
    @GetMapping("user/image/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable String filename) throws MalformedURLException {
        Path filePath = Paths.get("uploads").resolve(filename).normalize();
        org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @PostMapping("/user/uploadBase64")
    public ResponseEntity<String> uploadBase64(@RequestBody Map<String, String> body, Principal principal) throws IOException {
        String base64 = body.get("image").split(",")[1];
        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        Users user = usersRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = UUID.randomUUID() + ".jpg";
        Path filePath = Paths.get("uploads").resolve(fileName);
        Files.write(filePath, decodedBytes);

        user.setImage(fileName);
        usersRepo.save(user);

        return ResponseEntity.ok(fileName);
    }







}
