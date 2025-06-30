package tn.esprit.recommendstyle.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.recommendstyle.dto.ChangePassword;
import tn.esprit.recommendstyle.dto.MailBody;
import tn.esprit.recommendstyle.dto.OtpRequest;
import tn.esprit.recommendstyle.entity.ForgotPassword;
import tn.esprit.recommendstyle.entity.Users;
import tn.esprit.recommendstyle.repository.ForgotPasswordRepository;
import tn.esprit.recommendstyle.repository.UsersRepo;
import tn.esprit.recommendstyle.service.EmailService;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    // ✅ Étape 1 : Envoyer l'OTP par e-mail
    @PostMapping("/verifyMail")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody MailBody mailBody) {

        System.out.println("Reçu email : " + mailBody.to());

        String email = mailBody.to();

        Users users = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        forgotPasswordRepository.deleteByUser(users);
        int otp = otpGenerator();

        MailBody otpMail = MailBody.builder()
                .to(email)
                .subject("Password Reset OTP")
                .text("This is your OTP for password reset: " + otp)
                .build();

        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000)) // 70 sec expiration
                .user(users)
                .build();

        emailService.sendSimpleMessage(otpMail);
        forgotPasswordRepository.save(forgotPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to your email.");
        return ResponseEntity.ok(response);
    }

    // ✅ Étape 2 : Vérifier l'OTP
    @PostMapping("/verifyOtp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpRequest otpRequest) {
        String email = otpRequest.email();
        Integer otp = otpRequest.otp();

        System.out.println("🔐 Reçu OTP: " + otp + " pour email: " + email); // LOG
        Users users = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, users)
                .orElseThrow(() -> new RuntimeException("Invalid OTP")); // <-- Problème probable ici

        System.out.println("✅ OTP trouvé, expiration = " + fp.getExpirationTime());

        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getFpid());
            Map<String, String> response = Map.of("message", "OTP expired");
            return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
        }

        Map<String, String> response = Map.of("message", "OTP verified");
        return ResponseEntity.ok(response);
    }

    // ✅ Étape 3 : Changer le mot de passe
    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, String>> changePasswordHandler(@RequestBody ChangePassword changePassword) {
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            Map<String, String> response = Map.of("message", "Passwords do not match");
            return ResponseEntity.ok(response);

        }

        Users user = usersRepo.findByEmail(changePassword.email())
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        String encryptedPassword = passwordEncoder.encode(changePassword.password());
        usersRepo.updatePassword(changePassword.email(), encryptedPassword);

        Map<String, String> response = Map.of("message", "pass changee avec succes");
        return ResponseEntity.ok(response);
    }

    
    private Integer otpGenerator() {
        return new Random().nextInt(100_000, 999_999);
    }
}