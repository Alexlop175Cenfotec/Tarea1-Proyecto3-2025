package com.project.demo.rest.category;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Category> categoriesPage = categoryRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriesPage.getTotalPages());
        meta.setTotalElements(categoriesPage.getTotalElements());
        meta.setPageNumber(categoriesPage.getNumber() + 1);
        meta.setPageSize(categoriesPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Categories retrieved successfully",
                categoriesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> addCategory(@RequestBody Category category,
                                         HttpServletRequest request) {
        if (category != null) {
            Category savedCategory = categoryRepository.save(category);
            return new GlobalResponseHandler().handleResponse(
                    "Category created successfully",
                    savedCategory,
                    HttpStatus.CREATED,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Error creating Category",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,
                                            @RequestBody Category category,
                                            HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);

        if (foundCategory.isPresent()) {
            foundCategory.get().setId(categoryId);
            foundCategory.get().setNombre(category.getNombre());
            foundCategory.get().setDescripcion(category.getDescripcion());

            categoryRepository.save(foundCategory.get());
            return new GlobalResponseHandler().handleResponse(
                    "Category updated successfully",
                    category,
                    HttpStatus.OK,
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

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId,
                                            HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);

        if (foundCategory.isPresent()) {
            categoryRepository.deleteById(foundCategory.get().getId());
            return new GlobalResponseHandler().handleResponse(
                    "Category deleted successfully",
                    foundCategory.get(),
                    HttpStatus.OK,
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
}
