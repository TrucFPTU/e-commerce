package com.groupproject.ecommerce.service.impl;


import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.repository.UserRepository;
import com.groupproject.ecommerce.service.inter.AuthService;
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
}
