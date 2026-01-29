package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.request.ProfileUpdateReq;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.repository.UserRepository;
import com.groupproject.ecommerce.service.inter.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for User management
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User updateProfile(Long userId, ProfileUpdateReq req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (req.getFullname() != null && !req.getFullname().trim().isEmpty()) {
            u.setFullName(req.getFullname().trim());
        }

        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            u.setEmail(req.getEmail().trim());
        }

        // nếu user nhập newPassword thì mới đổi
        if (req.getNewPassword() != null && !req.getNewPassword().trim().isEmpty()) {
            u.setPassWord(passwordEncoder.encode(req.getNewPassword().trim()));
        }

        return userRepository.save(u);
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public User saveUser(Long userId, String email, String password, String fullName, Role role) {
        User user;
        
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        } else {
            user = new User();
            user.setRole(role);
        }
        
        user.setEmail(email);
        user.setFullName(fullName);
        
        // Only update password if provided
        if (password != null && !password.trim().isEmpty()) {
            user.setPassWord(passwordEncoder.encode(password));
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean emailExists(String email, Long excludeUserId) {
        return userRepository.findByEmail(email)
                .filter(user -> !user.getUserId().equals(excludeUserId))
                .isPresent();
    }
}
