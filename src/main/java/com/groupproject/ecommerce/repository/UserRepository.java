package com.groupproject.ecommerce.repository;

import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // Lấy danh sách staff để gán ngẫu nhiên / load list staff (nếu cần)
    List<User> findByRole(Role role);

    // Nhẹ hơn: chỉ lấy ID staff để random trong service (khuyến nghị)
    @Query("select u.userId from User u where u.role = com.groupproject.ecommerce.enums.Role.STAFF")
    List<Long> findAllStaffIds();
}
