package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sarapulov.demos.entities.UserChangeDTO;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.UserService;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
public class RestUserController {

    private UserService usersService;

    @PostMapping("/update-user")
    public ResponseEntity<Boolean> addUser(@AuthenticationPrincipal User user, @RequestBody UserChangeDTO userChangeDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to save updated user {}", user.getLogin());
        usersService.saveUserWithChanges(user, userChangeDTO);
        return ResponseEntity.ok(true);
    }

}
