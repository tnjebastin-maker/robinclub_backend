package com.example.e_commerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;
    private final EntityManager entityManager;

    public List<Product> getAll() { return productRepository.findAll(); }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> search(String q) { return productRepository.search(q); }

    @Transactional
    public Product save(Product product, List<MultipartFile> images) throws IOException {
        if (images != null) {
            List<String> newUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                    newUrls.add(uploadResult.get("secure_url").toString());
                }
            }
            if (!newUrls.isEmpty()) {
                if (product.getImageUrl() == null) product.setImageUrl(newUrls.get(0));
                product.getImages().addAll(newUrls);
            }
        }
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        entityManager.createQuery("UPDATE OrderItem oi SET oi.product = null WHERE oi.product.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        productRepository.deleteById(id);
    }
}
