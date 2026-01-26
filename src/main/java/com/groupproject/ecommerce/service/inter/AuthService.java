package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.request.RegisterRequest;
import com.groupproject.ecommerce.entity.User;

public interface AuthService {
    User login(String email, String rawPassword);
    User register(RegisterRequest req);
}
