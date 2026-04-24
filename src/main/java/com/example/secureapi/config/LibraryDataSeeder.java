package com.example.secureapi.config;

import com.example.secureapi.library.domain.Book;
import com.example.secureapi.library.repo.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LibraryDataSeeder {

    @Bean
    CommandLineRunner seedBooks(BookRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.save(new Book("Der alte Mann und das Meer", "Ernest Hemingway", "1952", "6366f1"));
            repo.save(new Book("1984", "George Orwell", "1949", "ef4444"));
            repo.save(new Book("Der Stachelritter", "Michael Ende", "2003", "22c55e"));
            repo.save(new Book("Clean Code", "Robert C. Martin", "2008", "eab308"));
            repo.save(new Book("Designing Data-Intensive Applications", "Martin Kleppmann", "2017", "a855f7"));
            repo.save(new Book("Eine kurze Geschichte der Zeit", "Stephen Hawking", "1988", "38bdf8"));
        };
    }
}
