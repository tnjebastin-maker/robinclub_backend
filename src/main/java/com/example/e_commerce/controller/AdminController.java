package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Product;
import com.example.e_commerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<Product> create(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer stock,
            @RequestParam String category,
            @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        return ResponseEntity.ok(productService.save(p, images));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer stock,
            @RequestParam String category,
            @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        Product p = productService.getById(id);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        return ResponseEntity.ok(productService.save(p, images));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
