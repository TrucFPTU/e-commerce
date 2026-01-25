package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.dto.request.ProfileUpdateReq;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.repository.UserRepository;
import com.groupproject.ecommerce.service.inter.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
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
}
