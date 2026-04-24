package com.example.secureapi.library.web;

import com.example.secureapi.library.service.LibraryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookController {

    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public String list(
            @AuthenticationPrincipal UserDetails user,
            Model model
    ) {
        model.addAttribute("books", libraryService.findAll());
        model.addAttribute("reader", user.getUsername());
        return "books";
    }

    @PostMapping("/{id}/borrow")
    public String borrow(
            @PathVariable long id,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes r
    ) {
        try {
            libraryService.borrow(id, user.getUsername());
            r.addFlashAttribute("ok", "Ausleihe bestätigt.");
        } catch (ResponseStatusException e) {
            r.addFlashAttribute("err", e.getReason());
        }
        return "redirect:/books";
    }

    @PostMapping("/{id}/return")
    public String returnBook(
            @PathVariable long id,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes r
    ) {
        try {
            libraryService.returnBook(id, user.getUsername());
            r.addFlashAttribute("ok", "Buch wurde zurückgegeben.");
        } catch (ResponseStatusException e) {
            r.addFlashAttribute("err", e.getReason());
        }
        return "redirect:/books";
    }
}
