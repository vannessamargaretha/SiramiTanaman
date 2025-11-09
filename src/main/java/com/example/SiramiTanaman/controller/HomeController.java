package com.example.SiramiTanaman.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/dashboard";
    }
}