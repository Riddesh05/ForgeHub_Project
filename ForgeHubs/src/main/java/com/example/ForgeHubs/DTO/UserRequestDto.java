package com.example.ForgeHubs.DTO;

import com.example.ForgeHubs.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    private String name ;
    private String email;
    private String password;
    private UserRole role;
}
