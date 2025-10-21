package com.project.demo.rest.category;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryRestController {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Category> data = categoryRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(data.getTotalPages());
        meta.setTotalElements(data.getTotalElements());
        meta.setPageNumber(data.getNumber() + 1);
        meta.setPageSize(data.getSize());

        return new GlobalResponseHandler().handleResponse("Categories retrieved", data.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> found = categoryRepository.findById(id);
        if (found.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Category retrieved", found.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category not found", HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> add(@RequestBody Category category, HttpServletRequest request) {
        Category saved = categoryRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created", saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category category, HttpServletRequest request) {
        Optional<Category> found = categoryRepository.findById(id);
        if (found.isPresent()) {
            Category c = found.get();
            c.setNombre(category.getNombre());
            c.setDescripcion(category.getDescripcion());
            Category saved = categoryRepository.save(c);
            return new GlobalResponseHandler().handleResponse("Category updated", saved, HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Category not found", HttpStatus.NOT_FOUND, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> found = categoryRepository.findById(id);
        if (found.isPresent()) {
            categoryRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse("Category deleted", found.get(), HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Category not found", HttpStatus.NOT_FOUND, request);
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProducts(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> found = categoryRepository.findById(id);
        if (found.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Products retrieved", found.get().getProducts(), HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Category not found", HttpStatus.NOT_FOUND, request);
    }

    @DeleteMapping("/{categoryId}/products/{productId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> deleteProduct(@PathVariable Long categoryId,
                                           @PathVariable Long productId,
                                           HttpServletRequest request) {
        Optional<Product> found = productRepository.findById(productId);
        if (found.isPresent() && found.get().getCategory() != null &&
                found.get().getCategory().getId().equals(categoryId)) {
            productRepository.delete(found.get());
            return new GlobalResponseHandler().handleResponse("Product deleted", found.get(), HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Product not found", HttpStatus.NOT_FOUND, request);
    }
}
