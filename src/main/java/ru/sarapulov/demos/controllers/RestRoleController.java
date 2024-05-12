package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sarapulov.demos.entities.ColumnToRoleAddingRequestDTO;
import ru.sarapulov.demos.entities.RoleChangeDTO;
import ru.sarapulov.demos.entities.RoleDTO;
import ru.sarapulov.demos.entities.RoleRequestDTO;
import ru.sarapulov.demos.entities.RoleSetDTO;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.RoleService;
import ru.sarapulov.demos.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
public class RestRoleController {

    private UserService usersService;

    private RoleService roleService;

    @PostMapping("/get-role")
    public ResponseEntity<RoleDTO> getRole(@AuthenticationPrincipal User user, @RequestBody RoleRequestDTO roleRequestDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to get role with id {} from team with id {}", roleRequestDTO.getRoleId(), roleRequestDTO.getTeamId());
        Role role = roleService.getRoleFromTeam(user, roleRequestDTO);
        RoleDTO roleDTO = RoleDTO.getDTOFrom(role);
        return ResponseEntity.ok(roleDTO);
    }

    @PostMapping("/update-role")
    public ResponseEntity<Boolean> updateRole(@AuthenticationPrincipal User user, @RequestBody RoleChangeDTO roleChangeDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to update role with id {} from team with id {}", roleChangeDTO.getRoleId(), roleChangeDTO.getTeamId());
        roleService.updateRoleFromTeam(user, roleChangeDTO);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/create-role")
    public ResponseEntity<UUID> createRole(@AuthenticationPrincipal User user, @RequestBody RoleChangeDTO roleChangeDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to create role from team with id {}", roleChangeDTO.getTeamId());
        return ResponseEntity.ok(roleService.createRoleForTeam(user, roleChangeDTO));
    }

    @PostMapping("/set-role")
    public ResponseEntity<Boolean> setRole(@AuthenticationPrincipal User user, @RequestBody RoleSetDTO roleSetDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to set role {} to team member {} from team with id {}",
                 roleSetDTO.getRoleId(),
                 roleSetDTO.getTeamMemberId(),
                 roleSetDTO.getTeamId());
        roleService.changeMemberRole(user, roleSetDTO);

        return ResponseEntity.ok(true);
    }

    @PostMapping("/add-allowed-column")
    public ResponseEntity<Boolean> getPossibleColumns(@AuthenticationPrincipal User user,
                                                      @RequestBody ColumnToRoleAddingRequestDTO columnAddingRequest) {
        user = usersService.updateUser(user);
        log.info("Trying to add to role {} column {} in team {}",
                 columnAddingRequest.getRoleId(),
                 columnAddingRequest.getColumnId(),
                 columnAddingRequest.getTeamId());
        roleService.addColumnToRole(user, columnAddingRequest);

        return ResponseEntity.ok(true);
    }

}
