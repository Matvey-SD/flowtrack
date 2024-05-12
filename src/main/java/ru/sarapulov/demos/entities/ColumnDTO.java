package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sarapulov.demos.models.Column;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ColumnDTO {

    private UUID id;

    private String columnName;

    public static ColumnDTO getDTOFrom(Column column) {
        return ColumnDTO.builder()
                        .id(column.getId())
                        .columnName(column.getName())
                        .build();
    }

}
