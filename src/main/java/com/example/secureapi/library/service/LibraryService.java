package com.example.secureapi.library.service;

import com.example.secureapi.library.domain.Book;
import com.example.secureapi.library.repo.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class LibraryService {

    private final BookRepository bookRepository;

    public LibraryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional
    public void borrow(long bookId, String readerName) {
        String name = normalizeReader(readerName);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buch nicht gefunden"));
        if (!book.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bereits ausgeliehen");
        }
        book.setBorrowedBy(name);
        book.setBorrowedAt(Instant.now());
    }

    @Transactional
    public void returnBook(long bookId, String readerName) {
        String name = normalizeReader(readerName);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buch nicht gefunden"));
        if (book.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nicht ausgeliehen");
        }
        if (!name.equalsIgnoreCase(book.getBorrowedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nur der Ausleiher kann zurückgeben");
        }
        book.setBorrowedBy(null);
        book.setBorrowedAt(null);
    }

    private static String normalizeReader(String readerName) {
        if (readerName == null || readerName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet");
        }
        return readerName.trim();
    }
}
