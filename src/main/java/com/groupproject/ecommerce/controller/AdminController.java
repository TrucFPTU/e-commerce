package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.entity.*;
import com.groupproject.ecommerce.enums.ProductStatus;
import com.groupproject.ecommerce.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final SupplierRepository supplierRepository;
    private final AuthorRepository authorRepository;

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ADMIN = "redirect:/admin";
    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    @GetMapping
    public String adminDashboard(Model model, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        populateModelWithData(model, user);
        return "admin/products";
    }

    @GetMapping("/products/{id}")
    @ResponseBody
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/products/save")
    public String saveProduct(
            @RequestParam(required = false) Long productId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) Integer publishYear,
            @RequestParam Integer stock,
            @RequestParam ProductStatus status,
            @RequestParam Long categoryId,
            @RequestParam Long publisherId,
            @RequestParam Long supplierId,
            @RequestParam List<Long> authorIds,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (getAuthenticatedUser(session) == null) {
            return REDIRECT_LOGIN;
        }

        try {
            Product product = findOrCreateProduct(productId);
            updateProductFields(product, name, description, imageUrl, price, publishYear, stock, status);
            setProductRelationships(product, categoryId, publisherId, supplierId, authorIds);
            productRepository.save(product);

            redirectAttributes.addFlashAttribute("message", 
                productId != null ? "Cập nhật sách thành công!" : "Thêm sách mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return REDIRECT_ADMIN;
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (getAuthenticatedUser(session) == null) {
            return REDIRECT_LOGIN;
        }

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            productRepository.delete(product);
            redirectAttributes.addFlashAttribute("message", "Xóa sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sách: " + e.getMessage());
        }

        return REDIRECT_ADMIN;
    }

    // Private helper methods
    private User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }

    private void populateModelWithData(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("publishers", publisherRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("authors", authorRepository.findAll());
    }

    private Product findOrCreateProduct(Long productId) {
        if (productId != null) {
            return productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        }
        return new Product();
    }

    private void updateProductFields(Product product, String name, String description, String imageUrl, 
                                     BigDecimal price, Integer publishYear, Integer stock, ProductStatus status) {
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setPrice(price);
        product.setPublishYear(publishYear);
        product.setStock(stock);
        product.setStatus(status);
    }

    private void setProductRelationships(Product product, Long categoryId, Long publisherId, 
                                         Long supplierId, List<Long> authorIds) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
        product.setCategory(category);

        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà xuất bản"));
        product.setPublisher(publisher);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));
        product.setSupplier(supplier);

        List<Author> authors = authorRepository.findAllById(authorIds);
        product.setAuthors(authors);
    }
}
