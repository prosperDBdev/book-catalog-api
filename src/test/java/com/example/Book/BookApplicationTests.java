package com.example.Book;

import com.example.Book.exception.ResourceNotFoundException;
import com.example.Book.model.Book;
import com.example.Book.repository.BookRepository;
import com.example.Book.service.BookServiceImpl;
import com.example.Book.util.BookJsonReader;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class BookApplicationTests {
    private final BookRepository repository = mock(BookRepository.class);
    private final BookServiceImpl service = new BookServiceImpl(repository);

    @Test
    void readsSampleWithStringIdsAndDates() throws Exception {
        try (var input = Files.newInputStream(Path.of("sample.json"))) {
            List<Book> books = BookJsonReader.read(input);
            assertEquals(2, books.size());
            assertEquals("bk101", books.getFirst().getId());
            assertEquals(LocalDate.of(2026, 10, 1), books.getFirst().getPublish_date());
            assertEquals("44.95", books.getFirst().getPrice().toPlainString());
            assertEquals("Ralls, Kim", books.get(1).getAuthor());
        }
    }

    @Test
    void rejectsInvalidJsonAndMissingCatalog() {
        for (String json : List.of("{", "{}", "{\"catalog\":{\"book\":[]}}")) {
            var exception = assertThrows(ResponseStatusException.class,
                    () -> BookJsonReader.read(input(json)));
            assertEquals(400, exception.getStatusCode().value());
        }
    }

    @Test
    void rejectsExistingId() {
        Book book = book("bk101");
        when(repository.existsById("bk101")).thenReturn(true);
        var exception = assertThrows(ResponseStatusException.class, () -> service.addBook(book));
        assertEquals(409, exception.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void allowsDifferentIdsWithSameTitle() {
        Book book = book("bk102");
        when(repository.save(book)).thenReturn(book);
        assertSame(book, service.addBook(book));
        verify(repository).save(book);
    }

    @Test
    void rejectsMissingId() {
        var exception = assertThrows(ResponseStatusException.class, () -> service.addBook(book(null)));
        assertEquals(400, exception.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void importsSampleInOneBatch() throws Exception {
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        try (var input = Files.newInputStream(Path.of("sample.json"))) {
            assertEquals(2, service.addBooks(input).size());
        }
        verify(repository).saveAll(anyList());
    }

    @Test
    void rejectsDuplicateIdsBeforeSaving() {
        String json = "{\"catalog\":{\"book\":["
                + "{\"id\":\"bk101\",\"author\":\"A\",\"title\":\"T\"},"
                + "{\"id\":\"bk101\",\"author\":\"B\",\"title\":\"U\"}]}}";
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.addBooks(input(json)));
        assertEquals(409, exception.getStatusCode().value());
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void deletesByStringId() {
        Book book = book("bk101");
        when(repository.findById("bk101")).thenReturn(Optional.of(book));
        service.deleteBook("bk101");
        verify(repository).delete(book);
    }

    @Test
    void rejectsMissingBookOnDelete() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteBook("missing"));
        verify(repository, never()).delete(any());
    }

    private static Book book(String id) {
        return new Book(id, "Author", "Title", "Fantasy", null, null, "Description");
    }

    private static ByteArrayInputStream input(String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }
}