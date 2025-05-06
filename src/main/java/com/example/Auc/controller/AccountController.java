package com.example.Auc.controller;

import com.example.Auc.entity.User;
import com.example.Auc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName());
    }

    @GetMapping("/myaccount")
    public String showMyAccount(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("fullName", user != null ? user.getFirstName() + " " + user.getLastName() : "");
        return "myaccount";
    }

    @PostMapping("/myaccount")
    public String saveProfile(@RequestParam("phone") String phone, Principal principal) {
        User user = getCurrentUser(principal);
        if (user != null) {
            user.setPhone(phone);
            userRepository.save(user);
        }
        return "redirect:/myaccount";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @RequestParam("current-password") String currentPassword,
            @RequestParam("new-password") String newPassword,
            @RequestParam("confirm-password") String confirmPassword,
            Principal principal,
            Model model
    ) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("passwordError", "Current password is incorrect.");
            model.addAttribute("user", user);
            return "myaccount";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("passwordError", "New passwords do not match.");
            model.addAttribute("user", user);
            return "myaccount";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        model.addAttribute("passwordSuccess", "Password updated successfully.");
        model.addAttribute("user", user);
        return "myaccount";
    }

    @GetMapping("/termsofservice")
    public String showTermsOfService() {
        return "termsofservice";
    }
}