package com.example.Book.service;

import com.example.Book.model.Book;

import java.io.InputStream;
import java.util.List;

public interface BookService {
    Book addBook(Book book);
    void deleteBook(String id);
    List<Book> addBooks(InputStream input);
}