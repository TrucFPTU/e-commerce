package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.entity.CartItem;
import com.groupproject.ecommerce.entity.User;

import java.util.List;

public interface CartService {
    CartItem addToCart(User user, Long productId, Integer quantity);
    List<CartItem> getCartItems(User user);
    void updateQuantity(Long cartItemId, Integer quantity);
    void removeCartItem(Long cartItemId);
    void clearCart(User user);
    int getCartCount(User user);
}