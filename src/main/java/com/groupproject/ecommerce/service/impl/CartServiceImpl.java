package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.Product;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.ProductStatus;
import com.groupproject.ecommerce.repository.CartItemRepository;
import com.groupproject.ecommerce.repository.ProductRepository;
import com.groupproject.ecommerce.service.inter.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public int getCartCount(User user) {
        Integer sum = cartItemRepository.sumQuantityByUser(user);
        return sum == null ? 0 : sum;
    }

    @Override
    @Transactional
    public CartItem addToCart(User user, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Sản phẩm không còn bán");
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException("Số lượng sản phẩm không đủ. Còn lại: " + product.getStock());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct_ProductId(user, productId);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Số lượng sản phẩm không đủ. Còn lại: " + product.getStock());
            }

            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPriceSnapshot(product.getPrice());
            return cartItemRepository.save(newItem);
        }
    }

    @Override
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Override
    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("Số lượng sản phẩm không đủ");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}