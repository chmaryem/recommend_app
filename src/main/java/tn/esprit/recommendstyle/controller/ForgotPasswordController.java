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
     ForgotPasswordRepository forgotPasswordRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email) {
      Users users=  usersRepo.findByEmail(email).
              orElseThrow(() ->  new UsernameNotFoundException("Please provide an valid email"));
int otp = otpGenerator();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("this is the otp for your Forgot Password request:"+ otp)
                .build();

        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis()+70*1000))
                .user(users)
                .build();
        emailService.sendSimpleMessage(mailBody);
        forgotPasswordRepository.save(fp);

        return ResponseEntity.ok("email sent for verification");

    }
    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        Users users=  usersRepo.findByEmail(email).
                orElseThrow(() ->  new UsernameNotFoundException("Please provide an valid email"));

       ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, users).
                orElseThrow(() -> new RuntimeException("Please provide an valid otp" +email ));

       if(fp.getExpirationTime().before(Date.from(Instant.now()))){
           forgotPasswordRepository.deleteById(fp.getFpid());
           return new ResponseEntity<>("OTP verified", HttpStatus.EXPECTATION_FAILED);
       }
       return ResponseEntity.ok("OTP verified");

    }
    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email) {
        if(!Objects.equals(changePassword.password(),changePassword.repeatPassword())){
            return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
        }
        String encryptedPassword = passwordEncoder.encode(changePassword.password());
        usersRepo.updatePassword(email, encryptedPassword);
        return ResponseEntity.ok("Password changed");
    }



    private Integer otpGenerator(){
        Random random = new Random();
        return random.nextInt(100_000,999999);

    }
}
