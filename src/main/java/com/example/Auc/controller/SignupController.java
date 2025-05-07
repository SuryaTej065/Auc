
//indulo password di requirement include chey em type chesina consider avtundi

package com.example.Auc.controller;

import com.example.Auc.entity.User;
import com.example.Auc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SignupController {

    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user, Model model) {

        String password = user.getPassword();
        if (password == null || password.length() < 8 || password.length() > 16 ||
                !password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")) {
            model.addAttribute("error", "Password must be 8-16 characters long and contain both letters and numbers.");
            model.addAttribute("user", user);
            return "signup";
        }

        String message = userService.registerUser(user);
        model.addAttribute("message", message);
        model.addAttribute("email", user.getEmail());

        if ("Email already exists and is verified!".equals(message)) {
            return "redirect:/login";
        }
        return "otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        if (userService.verifyOtp(email, otp)) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid or expired OTP. Please try again.");
            model.addAttribute("email", email);
            return "otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, Model model) {
        userService.resendOtp(email);
        model.addAttribute("message", "OTP has been resent to your email.");
        model.addAttribute("email", email);
        return "otp";
    }

    @GetMapping("/terms&conditions")
    public String terms() {
        return "terms&conditions";
    }
}