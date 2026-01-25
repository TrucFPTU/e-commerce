package com.groupproject.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProfileUpdateReq {
    private String fullname;
    private String email;
    private String newPassword;
}
