package ru.sarapulov.demos.services;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sarapulov.demos.models.User;

@Service
public class ModelService {
    public void setPageAttributes(Model model, String pageName, User user) {
        model.addAttribute("user", user);
        model.addAttribute("pageName", pageName);
    }
}
