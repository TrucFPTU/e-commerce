package com.groupproject.ecommerce.service.impl;


import com.groupproject.ecommerce.dto.request.RegisterRequest;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import com.groupproject.ecommerce.repository.UserRepository;
import com.groupproject.ecommerce.service.inter.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("email Người dùng không tồn tại hoặc chưa đăng ký"));
//        if (!passwordEncoder.matches(rawPassword, user.getPassWord())) {
//            throw new RuntimeException("Mật khẩu không đúng");
//        }
        if(!rawPassword.equals(user.getPassWord())){
            throw new RuntimeException("Mật khẩu không đúng");
        }
        return user;
    }

    @Override
    @Transactional
    public User register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        // 1) confirm password
        if (!req.getPassWord().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Nhập lại mật khẩu không khớp");
        }

        // 2) email unique
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 3) create user
        User u = new User();
        u.setFullName(req.getFullName().trim());
        u.setEmail(email);

        // ✅ tạm thời lưu plain text để test nhanh
        u.setPassWord(req.getPassWord());

        // default role
        u.setRole(Role.CUSTOMER);

        return userRepository.save(u);
    }
}
