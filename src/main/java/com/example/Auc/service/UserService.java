package com.example.Auc.service;

import com.example.Auc.entity.User;
import com.example.Auc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    public String registerUser(User user) {
        User existing = userRepository.findByEmail(user.getEmail());
        String otp = generateOtp();
        if (existing != null) {
            if (existing.isVerified()) {
                return "Email already exists and is verified!";
            } else {
                existing.setFirstName(user.getFirstName());
                existing.setLastName(user.getLastName());
                existing.setPassword(passwordEncoder.encode(user.getPassword()));  //spring security
                existing.setOtp(otp);
                existing.setOtpGeneratedAt(LocalDateTime.now());
                userRepository.save(existing);
                sendOtpEmail(existing.getEmail(), otp);
                return "OTP resent to your email.";
            }
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setOtp(otp);
            user.setOtpGeneratedAt(LocalDateTime.now());
            user.setVerified(false);
            userRepository.save(user);
            sendOtpEmail(user.getEmail(), otp);
            return "User registered successfully! Please verify your email.";
        }
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome to Mahindra University's Auction Community");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        if (user.isVerified()) return true;
        if (user.getOtp() == null || user.getOtpGeneratedAt() == null) return false;
        if (user.getOtp().equals(otp) && user.getOtpGeneratedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            user.setVerified(true);
            user.setOtp(null);
            user.setOtpGeneratedAt(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    //resend work avvatled check cheyyu once but also database lo kuda update avvali ala work ayyela chusko
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null && !user.isVerified()) {
            String otp = generateOtp();
            user.setOtp(otp);
            user.setOtpGeneratedAt(LocalDateTime.now());
            userRepository.save(user);
            sendOtpEmail(user.getEmail(), otp);
        }
    }
}