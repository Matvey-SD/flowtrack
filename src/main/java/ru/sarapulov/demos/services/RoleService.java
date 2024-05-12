package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.entities.ColumnToRoleAddingRequestDTO;
import ru.sarapulov.demos.entities.RoleChangeDTO;
import ru.sarapulov.demos.entities.RoleRequestDTO;
import ru.sarapulov.demos.entities.RoleSetDTO;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;
import ru.sarapulov.demos.models.Column;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.TeamMember;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.RolesRepository;
import ru.sarapulov.demos.repositories.TeamMembersRepository;
import ru.sarapulov.demos.repositories.TeamsRepository;
import ru.sarapulov.demos.utils.UserUtils;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class RoleService {

    private TeamsRepository teamsRepository;

    private RolesRepository rolesRepository;

    private TeamMembersRepository teamMembersRepository;

    public Role getRoleFromTeam(User requester, RoleRequestDTO roleRequest) {
        UUID teamId = roleRequest.getTeamId();
        UUID roleId = roleRequest.getRoleId();
        if (!UserUtils.isUserMemberOfTeam(requester, teamId)) {
            throw new UnauthorisedAccessException();
        }

        Team team = teamsRepository.findTeamById(teamId);
        Role role = rolesRepository.findById(roleId)
                                   .orElseThrow();

        if (team.getRoles()
                .stream()
                .noneMatch(role1 -> role1.getId()
                                         .compareTo(roleId) == 0)) {
            throw new UnauthorisedAccessException();
        }

        return role;
    }

    public void updateRoleFromTeam(User user, RoleChangeDTO roleChangeDTO) {
        Role requesterRole = UserUtils.getUserRoleInTeam(user, roleChangeDTO.getTeamId());
        Role role = getRoleIfPermittedOrThrow(requesterRole, roleChangeDTO.getTeamId(), roleChangeDTO.getRoleId());
        if (requesterRole.isOwner() && (role.isAdmin() || roleChangeDTO.isAdmin())) {
            role.setAdmin(roleChangeDTO.isAdmin());
        }

        if (requesterRole.isOwner()) {
            role.setRoleName(roleChangeDTO.getRoleName());
        }

        if (!role.isOwner()) {
            role.setRoleName(roleChangeDTO.getRoleName());
            role.setInvitingAvailable(roleChangeDTO.isInvitingAvailable());
            role.setCardEditAvailable(roleChangeDTO.isCardEditAvailable());
            role.setUserAssignationAvailable(roleChangeDTO.isUserAssignationAvailable());
            role.setColumnEditAvailable(roleChangeDTO.isColumnEditAvailable());
            role.setDocumentEditAvailable(roleChangeDTO.isDocumentEditAvailable());
        }

        rolesRepository.save(role);
    }

    private Role getRoleIfPermittedOrThrow(Role requesterRole, UUID teamId, UUID roleId) {
        Team team = teamsRepository.findTeamById(teamId);
        Role role = rolesRepository.findById(roleId)
                                   .orElseThrow();
        if (team.getRoles()
                .stream()
                .noneMatch(r -> r.getId()
                                 .compareTo(role.getId()) == 0)) {
            throw new UnauthorisedAccessException();
        }

        //Только админ и владелец может менять роли и админ не может изменять администраторские роли
        if (!(requesterRole.isOwner() || requesterRole.isAdmin()) || (!requesterRole.isOwner() && role.isAdmin())) {
            throw new UnauthorisedAccessException();
        }

        //Только создатель может менять роль создателя
        if (!requesterRole.isOwner() && role.isOwner()) {
            throw new UnauthorisedAccessException();
        }

        return role;
    }

    public UUID createRoleForTeam(User user, RoleChangeDTO roleDTO) {
        Role requesterRole = UserUtils.getUserRoleInTeam(user, roleDTO.getTeamId());
        if (!requesterRole.isOwner()) {
            throw new UnauthorisedAccessException();
        }
        UUID roleId = UUID.randomUUID();
        Role roleToCreate = Role.builder()
                                .id(roleId)
                                .roleName(roleDTO.getRoleName())
                                .permissions(roleDTO.getPermissions())
                                .team(requesterRole.getTeam())
                                .build();
        rolesRepository.save(roleToCreate);

        return roleId;
    }

    public void changeMemberRole(User user, RoleSetDTO roleSetDTO) {
        Role requesterRole = UserUtils.getUserRoleInTeam(user, roleSetDTO.getTeamId());
        Team team = UserUtils.findTeamForUser(user, roleSetDTO.getTeamId());
        TeamMember member = team.getMembers()
                                .stream()
                                .filter(mem -> mem.getId()
                                                  .compareTo(roleSetDTO.getTeamMemberId()) == 0)
                                .findFirst()
                                .orElseThrow();
        Role prevRole = member.getRole();
        Role assignedRole = team.getRoles()
                                .stream()
                                .filter(r -> r.getId()
                                              .compareTo(roleSetDTO.getRoleId()) == 0)
                                .findFirst()
                                .orElseThrow();
        if (prevRole.isOwner() || assignedRole.isOwner() || !(requesterRole.isOwner() || requesterRole.isAdmin())) {
            throw new UnauthorisedAccessException();
        }
        if (!(requesterRole.isOwner() || (requesterRole.isAdmin() && !(prevRole.isAdmin() || assignedRole.isAdmin())))) {
            throw new UnauthorisedAccessException();
        }

        member.setRole(assignedRole);
        teamMembersRepository.save(member);
    }

    public void addColumnToRole(User user, ColumnToRoleAddingRequestDTO columnAddingRequest) {
        Role requesterRole = UserUtils.getUserRoleInTeam(user, columnAddingRequest.getTeamId());
        Team team = requesterRole.getTeam();
        Role role = getRoleIfPermittedOrThrow(requesterRole, columnAddingRequest.getTeamId(), columnAddingRequest.getRoleId());

        Column columnToAdd = team.getColumns()
                                 .stream()
                                 .filter(column -> column.getId()
                                                         .compareTo(columnAddingRequest.getColumnId()) == 0)
                                 .findFirst()
                                 .orElseThrow();

        if (columnToAdd.getPermittedRoles() == null) {
            columnToAdd.setPermittedRoles(List.of(role));
        } else {
            List<Role> roles = columnToAdd.getPermittedRoles();
            roles.add(role);
            columnToAdd.setPermittedRoles(roles);
        }

        if (role.getColumnPermissions() == null) {
            role.setColumnPermissions(List.of(columnToAdd));
        }
        else if (role.getColumnPermissions()
                .stream()
                .anyMatch(column -> column.getId()
                                          .compareTo(columnToAdd.getId()) == 0)) {
            throw new UnauthorisedAccessException();
        } else {
            List<Column> columns = role.getColumnPermissions();
            columns.add(columnToAdd);
            role.setColumnPermissions(columns);
        }

        rolesRepository.save(role);
    }

}
