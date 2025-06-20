package tn.esprit.recommendstyle.controller;

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
import java.util.Date;
import java.util.Objects;
import java.util.Random;

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

    // ✅ Étape 1 : Envoyer l'OTP par e-mail
    @PostMapping("/verifyMail")
    public ResponseEntity<String> verifyEmail(@RequestBody MailBody mailBody) {
        String email = mailBody.to();

        Users users = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

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

        return ResponseEntity.ok("OTP sent to your email.");
    }

    // ✅ Étape 2 : Vérifier l'OTP
    @PostMapping("/verifyOtp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
        String email = otpRequest.email();
        Integer otp = otpRequest.otp();

        Users users = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, users)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getFpid());
            return new ResponseEntity<>("OTP expired", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified");
    }

    // ✅ Étape 3 : Changer le mot de passe
    @PostMapping("/changePassword")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword) {
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Passwords do not match", HttpStatus.EXPECTATION_FAILED);
        }

        Users user = usersRepo.findByEmail(changePassword.email())
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        String encryptedPassword = passwordEncoder.encode(changePassword.password());
        usersRepo.updatePassword(changePassword.email(), encryptedPassword);

        return ResponseEntity.ok("Password changed successfully");
    }

    // 🔐 Génération OTP à 6 chiffres
    private Integer otpGenerator() {
        return new Random().nextInt(100_000, 999_999);
    }
}