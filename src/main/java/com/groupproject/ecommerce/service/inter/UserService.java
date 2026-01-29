package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.request.ProfileUpdateReq;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;

import java.util.List;

/**
 * Service interface for User management
 */
public interface UserService {
    
    /**
     * Update user profile
     */
    User updateProfile(Long userId, ProfileUpdateReq req);
    
    /**
     * Get all users by role
     */
    List<User> getUsersByRole(Role role);
    
    /**
     * Get user by ID
     */
    User getUserById(Long id);
    
    /**
     * Save user (create or update)
     */
    User saveUser(Long userId, String email, String password, String fullName, Role role);
    
    /**
     * Delete user by ID
     */
    void deleteUser(Long id);
    
    /**
     * Check if email exists (excluding specific user)
     */
    boolean emailExists(String email, Long excludeUserId);
}
