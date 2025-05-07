package com.example.Auc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

// im commenting out since its already being used in auc cntrller
//    @GetMapping("/mybids")
//    public String showMyBidsPage() {
//        return "mybids";
//    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
}