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
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productsPage = productRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productsPage.getTotalPages());
        meta.setTotalElements(productsPage.getTotalElements());
        meta.setPageNumber(productsPage.getNumber() + 1);
        meta.setPageSize(productsPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Products retrieved successfully",
                productsPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @PostMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> addProductToCategory(@PathVariable Long categoryId,
                                                  @RequestBody Product product,
                                                  HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);

        if (foundCategory.isPresent()) {
            product.setCategory(foundCategory.get());
            Product savedProduct = productRepository.save(product);
            return new GlobalResponseHandler().handleResponse(
                    "Product created successfully",
                    savedProduct,
                    HttpStatus.CREATED,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Category id " + categoryId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                           @RequestBody Product product,
                                           HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(productId);

        if (foundProduct.isPresent()) {
            product.setId(foundProduct.get().getId());
            product.setCategory(foundProduct.get().getCategory());
            productRepository.save(product);
            return new GlobalResponseHandler().handleResponse(
                    "Product updated successfully",
                    product,
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId,
                                           HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(productId);

        if (foundProduct.isPresent()) {
            Optional<Category> category = categoryRepository.findById(foundProduct.get().getCategory().getId());
            category.ifPresent(c -> c.getProducts().remove(foundProduct.get()));

            productRepository.deleteById(foundProduct.get().getId());

            return new GlobalResponseHandler().handleResponse(
                    "Product deleted successfully",
                    foundProduct.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }
}
