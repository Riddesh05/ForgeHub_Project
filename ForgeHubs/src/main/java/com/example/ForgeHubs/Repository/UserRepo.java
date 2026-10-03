package com.example.ForgeHubs.Repository;


import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepo extends JpaRepository<User,Long> {
    User findByEmail(String email);
    List<User>  findByNameContaining(String name);
    List<User> findByRole(UserRole role);
}
