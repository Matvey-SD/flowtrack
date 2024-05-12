package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleChangeDTO {
    private UUID teamId;
    private UUID roleId;
    private String roleName;
    private boolean[] permissions;

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
}
