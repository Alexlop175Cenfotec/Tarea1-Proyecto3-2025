package com.project.demo.rest.product;

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
@RequestMapping("/products")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> data = productRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(data.getTotalPages());
        meta.setTotalElements(data.getTotalElements());
        meta.setPageNumber(data.getNumber() + 1);
        meta.setPageSize(data.getSize());

        return new GlobalResponseHandler().handleResponse("Products retrieved", data.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> found = productRepository.findById(id);
        if (found.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Product retrieved", found.get(), HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Product not found", HttpStatus.NOT_FOUND, request);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByCategory(@PathVariable Long categoryId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> data = productRepository.findByCategoryId(categoryId, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(data.getTotalPages());
        meta.setTotalElements(data.getTotalElements());
        meta.setPageNumber(data.getNumber() + 1);
        meta.setPageSize(data.getSize());

        return new GlobalResponseHandler().handleResponse("Products by category retrieved", data.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> add(@PathVariable Long categoryId,
                                 @RequestBody Product product,
                                 HttpServletRequest request) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {
            product.setCategory(category.get());
            Product saved = productRepository.save(product);
            return new GlobalResponseHandler().handleResponse("Product created", saved, HttpStatus.CREATED, request);
        }
        return new GlobalResponseHandler().handleResponse("Category not found", HttpStatus.NOT_FOUND, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Product product,
                                    HttpServletRequest request) {
        Optional<Product> found = productRepository.findById(id);
        if (found.isPresent()) {
            Product p = found.get();
            p.setNombre(product.getNombre());
            p.setDescripcion(product.getDescripcion());
            p.setPrecio(product.getPrecio());
            p.setStock(product.getStock());
            Product saved = productRepository.save(p);
            return new GlobalResponseHandler().handleResponse("Product updated", saved, HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Product not found", HttpStatus.NOT_FOUND, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> found = productRepository.findById(id);
        if (found.isPresent()) {
            productRepository.delete(found.get());
            return new GlobalResponseHandler().handleResponse("Product deleted", found.get(), HttpStatus.OK, request);
        }
        return new GlobalResponseHandler().handleResponse("Product not found", HttpStatus.NOT_FOUND, request);
    }
}
