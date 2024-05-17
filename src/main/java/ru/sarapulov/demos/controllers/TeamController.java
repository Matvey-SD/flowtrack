package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.ModelService;
import ru.sarapulov.demos.services.TeamService;
import ru.sarapulov.demos.services.UserService;
import ru.sarapulov.demos.utils.UserUtils;

import java.util.UUID;

@Controller
@AllArgsConstructor
public class TeamController {

    private TeamService teamService;

    private ModelService modelService;

    private UserService userService;

    @GetMapping("/team-creation")
    public String teamCreationPage(@AuthenticationPrincipal User user, Model model) {
        modelService.setPageAttributes(model, "Создание команды", user);
        return "team_creation_page";
    }

    @PostMapping("/team-creation")
    public String teamCreation(@AuthenticationPrincipal User user,
                               @RequestParam(name = "team-name") String teamName,
                               @RequestParam(name = "copy-checkbox", required = false) boolean isCopied,
                               @RequestParam(name = "copy-team", required = false) UUID teamToCopy,
                               @RequestParam(name = "copy-roles", required = false) boolean copyRoles,
                               @RequestParam(name = "copy-users", required = false) boolean copyUsers,
                               @RequestParam(name = "copy-columns", required = false) boolean copyColumns) {
        if (!isCopied) {
            teamService.createTeam(user, teamName);
        } else {
            teamService.createCopiedTeam(user, teamName, teamToCopy, copyRoles, copyUsers, copyColumns);
        }
        return "redirect:/home";
    }

    @GetMapping("/team")
    public String teamPage(@AuthenticationPrincipal User user, Model model, @RequestParam(name = "id") UUID id) {
        user = userService.updateUser(user);
        Team team = UserUtils.findTeamForUser(user, id);
        model.addAttribute("team", team);
        modelService.setPageAttributes(model, team.getTeam_name(), user);
        return "team_page";
    }

    @PostMapping("/delete-team")
    public String teamDeletion(@AuthenticationPrincipal User user, @RequestParam(name = "team-id") UUID teamId) {
        user = userService.updateUser(user);
        teamService.deleteTeam(user, teamId);
        return "redirect:/home";
    }

}
