package com.cloudaccesscontrol.controller;

import com.cloudaccesscontrol.model.User;
import com.cloudaccesscontrol.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String users(
            HttpSession session,
            Model model) {

        String role =
                (String) session.getAttribute("role");

        // Only ADMIN can access this page
        if (role == null ||
                !role.equalsIgnoreCase("ADMIN")) {

            return "redirect:/dashboard";
        }

        model.addAttribute(
                "users",
                userRepository.findAll()
        );

        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(
            @PathVariable Long id,
            @RequestParam String role,
            HttpSession session) {

        String adminRole =
                (String) session.getAttribute("role");

        // Only ADMIN can change roles
        if (adminRole == null ||
                !adminRole.equalsIgnoreCase("ADMIN")) {

            return "redirect:/dashboard";
        }

        User user =
                userRepository.findById(id)
                        .orElseThrow();

        if (role.equalsIgnoreCase("ADMIN") ||
                role.equalsIgnoreCase("USER")) {

            user.setRole(role.toUpperCase());

            userRepository.save(user);
        }

        return "redirect:/admin/users";
    }
}