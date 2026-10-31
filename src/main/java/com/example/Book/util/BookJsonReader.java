package com.example.Book.util;

import com.example.Book.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;

public final class BookJsonReader {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private BookJsonReader() {
    }

    public static List<Book> read(InputStream input) {
        Document document;
        try {
            document = MAPPER.readValue(input, Document.class);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid book JSON file", exception);
        }
        if (document == null || document.catalog() == null || document.catalog().book() == null
                || document.catalog().book().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON must contain a non-empty catalog.book array");
        }
        return document.catalog().book();
    }

    public record Document(Catalog catalog) {
    }

    public record Catalog(List<Book> book) {
    }
}