package com.daniil.booksonline.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BooksOnline API",
                version = "v1",
                description = "REST API for browsing products and purchasing books, software licenses, and vouchers.",
                contact = @Contact(name = "BooksOnline Team")
        )
)
public class OpenApiConfig {
}

