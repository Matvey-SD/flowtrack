package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sarapulov.demos.models.Role;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoleDTO {

    private String roleName;

    private boolean[] permissions;

    private List<ColumnDTO> columns;

    private List<ColumnDTO> columnsToAdd;

    public static RoleDTO getDTOFrom(Role role) {
        return RoleDTO.builder()
                      .roleName(role.getRoleName())
                      .permissions(role.getPermissions())
                      .columns(role.getColumnPermissions()
                                   .stream()
                                   .map(ColumnDTO::getDTOFrom)
                                   .toList())
                      .columnsToAdd(role.getTeam()
                                        .getColumns()
                                        .stream()
                                        .filter(col -> role.getColumnPermissions()
                                                           .stream()
                                                           .noneMatch(permittedCols -> col.getId()
                                                                                          .compareTo(permittedCols.getId()) == 0))
                                        .map(ColumnDTO::getDTOFrom)
                                        .toList())
                      .build();
    }

}
