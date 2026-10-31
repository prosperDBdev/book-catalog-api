package com.example.Book.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Book {
    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String title;

    private String genre;

    private BigDecimal price;

    @Column(name = "publish_date")
    private LocalDate publish_date;

    private String description;
}