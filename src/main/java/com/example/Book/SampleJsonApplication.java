package com.example.Book;

import com.example.Book.model.Book;
import com.example.Book.util.BookJsonReader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SampleJsonApplication {
    public static void main(String[] args) throws Exception {
        Path path = Path.of(args.length == 0 ? "sample.json" : args[0]);
        try (InputStream input = Files.newInputStream(path)) {
            for (Book book : BookJsonReader.read(input)) {
                System.out.println("id: " + book.getId());
                System.out.println("author: " + book.getAuthor());
                System.out.println("title: " + book.getTitle());
                System.out.println("genre: " + book.getGenre());
                System.out.println("price: " + book.getPrice());
                System.out.println("publish_date: " + book.getPublish_date());
                System.out.println("description: " + book.getDescription());
                System.out.println();
            }
        }
    }
}