package tn.esprit.recommendstyle.controller;

import com.twilio.base.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.recommendstyle.dto.ReqRes;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.repository.UsersRepo;
import tn.esprit.recommendstyle.service.FileStorageService;
import tn.esprit.recommendstyle.service.UserManagementService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes reg) {
        ReqRes result = usersManagementService.register(reg);
        return ResponseEntity.status(result.getStatusCode()).body(result);
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

    @PostMapping("/user/uploadBase64WithAnalysis")
    public ResponseEntity<Map<String, Object>> uploadBase64WithAnalysis(@RequestBody Map<String, String> body,
                                                                        Principal principal) throws IOException {
        // 🔓 Extraction de l'image base64
        String base64 = body.get("image").split(",")[1];
        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        // 👤 Récupération de l'utilisateur
        Users user = usersRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 📂 Sauvegarde de l'image
        String fileName = UUID.randomUUID() + ".jpg";
        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir); // assure que le dossier existe
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, decodedBytes);
        BufferedImage img = ImageIO.read(filePath.toFile());
        System.out.println("📐 Taille image enregistrée : " + img.getWidth() + "x" + img.getHeight());
        // 📝 Mise à jour utilisateur
        user.setImage(fileName);
        usersRepo.save(user);

        // 🚀 Appel à FastAPI avec chemin compatible (Linux-style)
        String unixPath = filePath.toAbsolutePath().toString().replace("\\", "/");

        // 🔁 Préparer la requête
        Map<String, String> fastApiRequest = Map.of("image_url", unixPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(fastApiRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        // 🔍 Appel FastAPI
        Map<String, Object> fastApiResponse = restTemplate.postForObject(
                "http://localhost:8087/analyze-image-url", requestEntity, Map.class);

        // 🧠 Fusion de la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("emotion", fastApiResponse.get("emotion"));
        response.put("confidence", fastApiResponse.get("confidence"));

        return ResponseEntity.ok(response);
    }





}
