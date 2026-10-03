package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.UserCreateRequest;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/create")
    public String createUser(
            @Valid @ModelAttribute("userRequest")
            UserCreateRequest request,

            BindingResult bindingResult,

            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "users",
                    userService.getAllUsers()
            );

            return "admin/users";
        }

        try {

            userService.createVendor(request);

            return "redirect:/admin/users?success";

        } catch (RuntimeException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "users",
                    userService.getAllUsers()
            );

            return "admin/users";
        }
    }
}