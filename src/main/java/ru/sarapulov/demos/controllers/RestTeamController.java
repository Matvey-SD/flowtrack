package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sarapulov.demos.entities.ColumnAddingRequestDTO;
import ru.sarapulov.demos.entities.ColumnDeletionDTO;
import ru.sarapulov.demos.entities.ColumnPositionChangingDTO;
import ru.sarapulov.demos.entities.UserAddingRequestDTO;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.TeamService;
import ru.sarapulov.demos.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
public class RestTeamController {

    private TeamService teamService;

    private UserService usersService;

    @PostMapping("/add-user")
    public ResponseEntity<Boolean> addUser(@AuthenticationPrincipal User user,
                                           @RequestBody UserAddingRequestDTO userAddingRequestDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to add user {} to team with id {}", userAddingRequestDTO.getLogin(), userAddingRequestDTO.getTeamId());
        teamService.addUserToTeam(user, userAddingRequestDTO.getLogin(), userAddingRequestDTO.getTeamId());
        return ResponseEntity.ok(true);
    }

    @PostMapping("/add-column")
    public ResponseEntity<UUID> addColumn(@AuthenticationPrincipal User user,
                                          @RequestBody ColumnAddingRequestDTO columnAddingRequestDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to add column {} to team with id {}",
                 columnAddingRequestDTO.getColumnName(),
                 columnAddingRequestDTO.getTeamId());
        UUID savedId = teamService.addColumnToTeam(user, columnAddingRequestDTO);
        return ResponseEntity.ok(savedId);
    }

    @PostMapping("update-column-position")
    public ResponseEntity<Boolean> updateColumnPosition(@AuthenticationPrincipal User user,
                                                        @RequestBody ColumnPositionChangingDTO positionChangingDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to put column {} in team {} to {} position",
                 positionChangingDTO.getColumnId(),
                 positionChangingDTO.getTeamId(),
                 positionChangingDTO.getPosition());
        teamService.changeColumnPosition(user, positionChangingDTO);

        return ResponseEntity.ok(true);
    }

    @PostMapping("delete-column")
    public ResponseEntity<Boolean> deleteColumn(@AuthenticationPrincipal User user,
                                                @RequestBody ColumnDeletionDTO columnDeletionDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to delete column {} in team {}", columnDeletionDTO.getColumnId(), columnDeletionDTO.getTeamId());
        teamService.deleteColumn(user, columnDeletionDTO);

        return ResponseEntity.ok(true);
    }

}
