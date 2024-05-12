package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;
import ru.sarapulov.demos.models.Column;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.TeamMember;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.TeamMembersRepository;
import ru.sarapulov.demos.repositories.TeamsRepository;
import ru.sarapulov.demos.repositories.UsersRepository;
import ru.sarapulov.demos.utils.UserUtils;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TeamService {

    private TeamsRepository teamsRepository;

    private TeamMembersRepository teamMembersRepository;

    private UsersRepository usersRepository;

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
    }

    public UUID addColumnToTeam(User requester, String columnName, UUID teamId) {
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, teamId);
        if (!requesterRole.isColumnEditAvailable()) {
            throw new UnauthorisedAccessException();
        }
        UUID savedId = UUID.randomUUID();
        Team team = teamsRepository.findTeamById(teamId);
        Column columnToAdd = Column.builder()
                                   .name(columnName)
                                   .team(team)
                                   .id(savedId)
                                   .build();
        team.getColumns()
            .add(columnToAdd);
        teamsRepository.save(team);
        return savedId;
    }

    public void deleteTeam(User requester, UUID teamId) {
        TeamMember requesterMembership = UserUtils.getUserMembershipInTeam(requester, teamId);
        if (!requesterMembership.getRole()
                                .isOwner()) {
            throw new UnauthorisedAccessException();
        }
        Team teamToDelete = requesterMembership.getTeam();

        teamMembersRepository.deleteAll(teamToDelete.getMembers());
        teamsRepository.delete(teamToDelete);

    }

}
