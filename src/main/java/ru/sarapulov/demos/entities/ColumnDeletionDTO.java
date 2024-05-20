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
public class ColumnDeletionDTO {
    private UUID teamId;
    private UUID columnId;
}
