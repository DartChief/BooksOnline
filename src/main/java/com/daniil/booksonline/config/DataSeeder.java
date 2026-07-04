package com.daniil.booksonline.config;

import com.daniil.booksonline.entity.Product;
import com.daniil.booksonline.entity.ProductType;
import com.daniil.booksonline.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            log.info("Seeding demo products for BooksOnline");

            List<Product> products = List.of(
                    new Product("E-Commerce done right", "Book about practical webshop delivery", new BigDecimal("49.99"), 12, ProductType.BOOK),
                    new Product("E-Commerce simulator", "Digital license for the E-Commerce simulator software", new BigDecimal("199.00"), 5, ProductType.SOFTWARE_LICENSE),
                    new Product("Voucher", "Voucher redeemable in the BooksOnline store", new BigDecimal("100.00"), 20, ProductType.VOUCHER)
            );

            productRepository.saveAll(products);
        };
    }
}
