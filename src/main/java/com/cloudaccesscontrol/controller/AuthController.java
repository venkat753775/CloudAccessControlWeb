package com.cloudaccesscontrol.controller;

import com.cloudaccesscontrol.model.User;
import com.cloudaccesscontrol.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if (userRepository.findByUsername(username).isPresent()) {

            model.addAttribute(
                    "error",
                    "Username already exists."
            );

            return "register";
        }

        User user = new User(
                username,
                password,
                "USER"
        );

        userRepository.save(user);

        model.addAttribute(
                "message",
                "Registration successful! Please login."
        );

        return "login";
    }

    // Login
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        var userOptional =
                userRepository.findByUsername(username);

        if (userOptional.isPresent()) {

            User user = userOptional.get();

            if (user.getPassword().equals(password)) {

                // Store logged-in user information in session
                session.setAttribute(
                        "username",
                        user.getUsername()
                );

                session.setAttribute(
                        "role",
                        user.getRole()
                );

                return "redirect:/dashboard";
            }
        }

        model.addAttribute(
                "error",
                "Invalid username or password."
        );

        return "login";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}