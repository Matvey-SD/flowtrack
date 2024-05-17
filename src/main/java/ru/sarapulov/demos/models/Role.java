package ru.sarapulov.demos.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Role {

    @Id
    private UUID id;

    private String roleName;

    private boolean[] permissions = new boolean[8];

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Team team;

    @OneToMany(mappedBy = "role")
    private List<TeamMember> teamMember;

    @ManyToMany(mappedBy = "permittedRoles", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Column> columnPermissions;

    public static Role createOwnerRole() {
        return new RoleBuilder().roleName("OWNER")
                                .permissions(new boolean[] {true, true, true, true, true, true, true, false})
                                .id(UUID.randomUUID())
                                .build();
    }

    public static Role createObserverRole(Team team) {
        return new RoleBuilder().roleName("OBSERVER")
                                .permissions(new boolean[] {false, false, false, false, false, false, false, true})
                                .team(team)
                                .id(UUID.randomUUID())
                                .build();
    }

    public boolean isCardCreationAvailableInColumn(Column column) {
        return isCardEditAvailable() || columnPermissions.stream()
                                                         .anyMatch(col -> column.getId()
                                                                                .compareTo(col.getId()) == 0);
    }

    public boolean isOwner() {
        return permissions[0];
    }

    public boolean isAdmin() {
        return permissions[1];
    }

    public boolean isInvitingAvailable() {
        return permissions[2];
    }

    public boolean isCardEditAvailable() {
        return permissions[3];
    }

    public boolean isUserAssignationAvailable() {
        return permissions[4];
    }

    public boolean isColumnEditAvailable() {
        return permissions[5];
    }

    public boolean isDocumentEditAvailable() {
        return permissions[6];
    }

    public boolean isDefaultRole() {
        return permissions[7];
    }

    public void setAdmin(boolean f) {
        permissions[1] = f;
    }

    public void setInvitingAvailable(boolean f) {
        permissions[2] = f;
    }

    public void setCardEditAvailable(boolean f) {
        permissions[3] = f;
    }

    public void setUserAssignationAvailable(boolean f) {
        permissions[4] = f;
    }

    public void setColumnEditAvailable(boolean f) {
        permissions[5] = f;
    }

    public void setDocumentEditAvailable(boolean f) {
        permissions[6] = f;
    }

    public static Role copyRole(Role role) {
        return Role.builder()
                   .roleName(role.getRoleName())
                   .permissions(role.getPermissions())
                   .id(UUID.randomUUID())
                   .build();
    }

    public static Role copyRoleToTeam(Role role, Team team) {
        return Role.builder()
                   .roleName(role.getRoleName())
                   .permissions(role.getPermissions())
                   .id(UUID.randomUUID())
                   .team(team)
                   .build();
    }

}
