package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.entities.ColumnAddingRequestDTO;
import ru.sarapulov.demos.entities.ColumnDeletionDTO;
import ru.sarapulov.demos.entities.ColumnPositionChangingDTO;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;
import ru.sarapulov.demos.models.Column;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.TeamMember;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.ColumnsRepository;
import ru.sarapulov.demos.repositories.RolesRepository;
import ru.sarapulov.demos.repositories.TeamMembersRepository;
import ru.sarapulov.demos.repositories.TeamsRepository;
import ru.sarapulov.demos.repositories.UsersRepository;
import ru.sarapulov.demos.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TeamService {

    private TeamsRepository teamsRepository;

    private TeamMembersRepository teamMembersRepository;

    private RolesRepository rolesRepository;

    private DocumentService documentService;

    private ColumnsRepository columnsRepository;

    private CardService cardService;

    private UsersRepository usersRepository;

    private MailSendService mailSendService;

    public void createTeam(User user, String teamName) {
        TeamMember owner = TeamMember.createOwner(user);
        Team team = Team.createTeam(teamName, owner);
        owner.setTeam(team);
        owner.getRole()
             .setTeam(team);
        Role defaultRole = Role.createObserverRole(team);
        team.setRoles(List.of(defaultRole, owner.getRole()));
        log.info("Creating new team: {}, User: {}", teamName, user.getLogin());
        teamsRepository.save(team);
    }

    public void createCopiedTeam(User user,
                                 String teamName,
                                 UUID teamToCopy,
                                 boolean copyRoles,
                                 boolean copyUsers,
                                 boolean copyColumns) {
        Team copyTeam = UserUtils.findTeamForUser(user, teamToCopy);
        TeamMember owner;
        if (copyRoles) {
            owner = TeamMember.copyCreateOwner(user, copyTeam.getOwnerRole());
        } else {
            owner = TeamMember.createOwner(user);
        }
        Team team = Team.createTeam(teamName, owner);
        owner.setTeam(team);
        owner.getRole()
             .setTeam(team);
        if (copyRoles) {
            List<Role> rolesToAdd = new ArrayList<>(List.of(owner.getRole()));
            for (Role role : copyTeam.getRoles()) {
                if (role.isOwner()) {
                    continue;
                }
                Role copiedRole = Role.copyRoleToTeam(role, team);
                rolesToAdd.add(copiedRole);
            }
            team.setRoles(rolesToAdd);
        } else {
            Role defaultRole = Role.createObserverRole(team);
            team.setRoles(List.of(defaultRole, owner.getRole()));
        }

        if (copyUsers) {
            List<TeamMember> members = new ArrayList<>(List.of(owner));
            for (TeamMember member : copyTeam.getMembers()) {
                if (member.getUser()
                          .getLogin()
                          .equals(user.getLogin())) {
                    continue;
                }
                TeamMember addedUser = TeamMember.builder()
                                                 .user(member.getUser())
                                                 .team(team)
                                                 .role(team.getDefaultRole())
                                                 .build();
                members.add(addedUser);
            }
            team.setMembers(members);
        }

        if (copyColumns) {
            List<Column> columns = new ArrayList<>();
            for (Column column : copyTeam.getColumns()) {
                columns.add(Column.copyColumn(column, team));
            }
            team.setColumns(columns);
        }

        teamsRepository.save(team);
    }


    public void addUserToTeam(User requester, String login, UUID teamId) {
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, teamId);
        if (!requesterRole.isInvitingAvailable()) {
            throw new UnauthorisedAccessException();
        }
        User user = usersRepository.findUserByLogin(login);
        Team team = teamsRepository.findTeamById(teamId);

        TeamMember addedUser = TeamMember.builder()
                                         .user(user)
                                         .team(team)
                                         .role(team.getDefaultRole())
                                         .build();
        team.getMembers()
            .add(addedUser);
        teamsRepository.save(team);
        mailSendService.sendMessageToUserIfPossible(user,
                                                    String.format("Приглашение в команду %s", team.getTeam_name()),
                                                    String.format("Вы были приглашены в команду %s", team.getTeam_name()));
    }

    public UUID addColumnToTeam(User requester, ColumnAddingRequestDTO columnAddingRequest) {
        UUID teamId = columnAddingRequest.getTeamId();
        String columnName = columnAddingRequest.getColumnName();
        int columnType = columnAddingRequest.getColumnType();
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, teamId);
        if (!requesterRole.isColumnEditAvailable()) {
            throw new UnauthorisedAccessException();
        }
        UUID savedId = UUID.randomUUID();
        Team team = requesterRole.getTeam();
        int columnPosition = team.getColumns()
                                 .size();
        Column columnToAdd = Column.builder()
                                   .name(columnName)
                                   .team(team)
                                   .columnType(columnType)
                                   .id(savedId)
                                   .position(columnPosition)
                                   .build();
        team.getColumns()
            .add(columnToAdd);
        teamsRepository.save(team);
        return savedId;
    }

    public void changeColumnPosition(User requester, ColumnPositionChangingDTO positionChangingDTO) {
        UUID teamId = positionChangingDTO.getTeamId();
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, teamId);
        if (!requesterRole.isColumnEditAvailable()) {
            throw new UnauthorisedAccessException();
        }
        Team team = requesterRole.getTeam();
        int newPos = positionChangingDTO.getPosition();
        List<Column> columns = team.getSortedColumns();
        int prevPos = columns.stream()
                             .filter(col -> col.getId()
                                               .compareTo(positionChangingDTO.getColumnId()) == 0)
                             .findFirst()
                             .orElseThrow()
                             .getPosition();
        if (prevPos == newPos) {
            return;
        }
        if (newPos > prevPos) {
            for (int i = prevPos + 1; i < newPos + 1; i++) {
                columns.get(i)
                       .setPosition(i - 1);
            }
            columns.get(prevPos)
                   .setPosition(newPos);
        }
        if (newPos < prevPos) {
            for (int i = newPos; i < prevPos; i++) {
                columns.get(i)
                       .setPosition(i + 1);
            }
            columns.get(prevPos)
                   .setPosition(newPos);
        }

        teamsRepository.save(team);
    }

    public void deleteColumn(User requester, ColumnDeletionDTO deletionDTO) {
        UUID teamId = deletionDTO.getTeamId();
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, teamId);
        if (!requesterRole.isColumnEditAvailable()) {
            throw new UnauthorisedAccessException();
        }
        Column columnToDelete = requesterRole.getTeam()
                                             .getColumn(deletionDTO.getColumnId());
        cardService.deleteCards(columnToDelete.getCards());
        columnsRepository.delete(columnToDelete);
    }

    public void deleteTeam(User requester, UUID teamId) {
        TeamMember requesterMembership = UserUtils.getUserMembershipInTeam(requester, teamId);
        if (!requesterMembership.getRole()
                                .isOwner()) {
            throw new UnauthorisedAccessException();
        }
        Team teamToDelete = requesterMembership.getTeam();

        teamMembersRepository.deleteAll(teamToDelete.getMembers());
        cardService.deleteCards(teamToDelete.getAllTeamCards());
        columnsRepository.deleteAll(teamToDelete.getColumns());
        documentService.deleteAll(teamToDelete.getDocuments());
        rolesRepository.deleteAll(teamToDelete.getRoles());
        teamsRepository.delete(teamToDelete);

        teamToDelete.getMembers()
                    .stream()
                    .map(TeamMember::getUser)
                    .forEach(user -> notifyDeletion(user, teamToDelete));
    }

    private void notifyDeletion(User user, Team teamToDelete) {
        mailSendService.sendMessageToUserIfPossible(user,
                                                    String.format("Удаление команды %s", teamToDelete.getTeam_name()),
                                                    String.format(
                                                        "Информируем вас о том, что команда %s, в которой вы состояли, была удалена.",
                                                        teamToDelete.getTeam_name()));
    }

}
