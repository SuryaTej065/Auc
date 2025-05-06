package com.example.Auc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

//    @GetMapping("/mybids")
//    public String showMyBidsPage() {
//        return "mybids"; // Looks for mybids.html in templates
//    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home"; // Looks for home.html in templates
    }
}