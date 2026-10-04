package com.example.ForgeHubs.Repository;


import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
//    User findByEmail(String email);
Optional<User> findByEmail(String email);
//    User findByEmailAndPassword(String email, String password);
//    List<User>  findByNameContaining(String name);
    List<User> findByRole(UserRole role);
    boolean existsByEmail(String email);
    Optional<User> findByName(String Name);
}
