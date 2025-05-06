// src/main/java/com/example/Auc/controller/LoginController.java
package com.example.Auc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Handles GET requests to /login (serves the login page)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Looks for login.html in src/main/resources/templates/
    }
}