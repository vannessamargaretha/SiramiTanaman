package com.example.SiramiTanaman.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.SiramiTanaman.model.User;
import com.example.SiramiTanaman.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(Principal principal, Model model) {

        return "auth/login"; 
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username sudah digunakan.");
            return "auth/register";
        }

        userService.registerUser(user); 
        model.addAttribute("success", "Akun berhasil dibuat. Silakan login.");

        return "redirect:/login";
    }
}
