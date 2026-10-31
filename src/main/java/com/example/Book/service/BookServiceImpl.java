package com.example.Book.service;

import com.example.Book.exception.ResourceNotFoundException;
import com.example.Book.model.Book;
import com.example.Book.repository.BookRepository;
import com.example.Book.util.BookJsonReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public Book addBook(Book book) {
        validateBook(book);
        checkExistingId(book.getId());
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public List<Book> addBooks(InputStream input) {
        List<Book> books = BookJsonReader.read(input);
        Set<String> ids = new HashSet<>();
        for (Book book : books) {
            validateBook(book);
            if (!ids.add(book.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Duplicate book id in file: " + book.getId());
            }
            checkExistingId(book.getId());
        }
        return bookRepository.saveAll(books);
    }

    @Override
    @Transactional
    public void deleteBook(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        bookRepository.delete(book);
    }

    private void checkExistingId(String id) {
        if (bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book already exists with id: " + id);
        }
    }

    private void validateBook(Book book) {
        if (book == null || book.getId() == null || book.getId().isBlank()
                || book.getAuthor() == null || book.getAuthor().isBlank()
                || book.getTitle() == null || book.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book id, author and title are required");
        }
    }
}