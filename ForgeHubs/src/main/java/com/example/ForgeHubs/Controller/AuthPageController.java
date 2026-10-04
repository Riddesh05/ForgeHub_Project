package com.example.ForgeHubs.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/")
    public String loginPage() {
        return "auth/login";
    }

        @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/setup-2fa")
    public String setup2faPage() {
        return "auth/setup-2fa";
    }

    @GetMapping("/verify-2fa")
    public String verify2faPage() {
        return "auth/verify-2fa";
    }

    @GetMapping("/recover-2fa")
    public String recover2faPage() {
        return "auth/recover-2fa";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "auth/dashboard";
    }
}