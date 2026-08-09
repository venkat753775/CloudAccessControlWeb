package com.cloudaccesscontrol.controller;

import com.cloudaccesscontrol.model.FileRecord;
import com.cloudaccesscontrol.repository.FileRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final FileRepository fileRepository;

    public HomeController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        String username =
                (String) session.getAttribute("username");

        String role =
                (String) session.getAttribute("role");

        // User must be logged in
        if (username == null) {
            return "redirect:/login";
        }

        List<FileRecord> files;

        // ADMIN can see all files
        if ("ADMIN".equalsIgnoreCase(role)) {

            files = fileRepository.findAll();

        } else {

            // USER can see only their own files
            files = fileRepository.findByUsername(username);
        }

        model.addAttribute("files", files);

        return "dashboard";
    }

    @GetMapping("/decrypt")
public String decrypt(
        HttpSession session,
        Model model) {

    String username =
            (String) session.getAttribute("username");

    String role =
            (String) session.getAttribute("role");

    if (username == null) {
        return "redirect:/login";
    }

    if ("ADMIN".equalsIgnoreCase(role)) {

        model.addAttribute(
                "files",
                fileRepository.findAll()
        );

    } else {

        model.addAttribute(
                "files",
                fileRepository.findByUsername(username)
        );
    }

    return "decrypt";
}
}