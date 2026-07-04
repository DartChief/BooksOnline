package com.daniil.booksonline;

import org.springframework.boot.SpringApplication;

public class TestBooksOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.from(BooksOnlineApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
