package com.groupproject.ecommerce.service.inter;

import com.groupproject.ecommerce.dto.request.ProfileUpdateReq;
import com.groupproject.ecommerce.entity.User;

public interface UserService {
    User updateProfile(Long userId, ProfileUpdateReq req);
}
