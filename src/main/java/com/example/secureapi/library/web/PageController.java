package com.example.secureapi.library.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/books";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(
            Authentication auth,
            @RequestParam(name = "logout", required = false) String logout,
            @RequestParam(name = "error", required = false) String error,
            Model m
    ) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/books";
        }
        if (logout != null) {
            m.addAttribute("message", "Du wurdest abgemeldet.");
        }
        if (error != null) {
            m.addAttribute("err", "Anmeldung fehlgeschlagen. Bitte einen Namen eintragen.");
        }
        return "login";
    }
}
