package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.ModelService;
import ru.sarapulov.demos.services.UserService;

@Controller
@AllArgsConstructor
public class UserController {

    private ModelService modelService;

    private UserService userService;

    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal User user, Model model) {
        user = userService.updateUser(user);
        modelService.setPageAttributes(model, "Домашняя страница", user);
        return "home_page";
    }

    @GetMapping("/")
    public String signRedirect() {
        return "redirect:/home";
    }

    @GetMapping("/signIn")
    public String signIn() {
        return "signin_page";
    }

    @GetMapping("/signUp")
    public String signUp() {
        return "signup_page";
    }

    @PostMapping("/signUp")
    public String registerUser(User user) {
        boolean userIsRegistered = userService.tryRegisterUser(user);
        return userIsRegistered ? "redirect:/logout" : "failed_registration_page";
    }

}
