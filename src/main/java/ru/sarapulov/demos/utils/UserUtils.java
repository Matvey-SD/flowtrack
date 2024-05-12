package ru.sarapulov.demos.utils;

import ru.sarapulov.demos.exceptions.TeamNotFoundException;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.TeamMember;
import ru.sarapulov.demos.models.User;

import java.util.UUID;

public class UserUtils {
    public static boolean isUserMemberOfTeam(User user, UUID teamId) {
        for (TeamMember member : user.getTeams()) {
            if (member.getTeam()
                      .getId()
                      .compareTo(teamId) == 0) {
                return true;
            }
        }

        return false;
    }

    public static Role getUserRoleInTeam(User user, UUID teamId) {
        return getUserMembershipInTeam(user, teamId).getRole();
    }

    public static Team findTeamForUser(User user, UUID teamId) {
        return getUserMembershipInTeam(user, teamId).getTeam();
    }

    public static TeamMember getUserMembershipInTeam(User user, UUID teamId) {
        for (TeamMember member : user.getTeams()) {
            if (member.getTeam()
                      .getId()
                      .compareTo(teamId) == 0) {
                return member;
            }
        }
        throw new TeamNotFoundException();
    }
}
