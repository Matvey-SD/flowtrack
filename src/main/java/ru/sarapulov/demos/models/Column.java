package ru.sarapulov.demos.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "columns")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Column {

    @Id
    private UUID id;

    private String name;

    private int columnType;

    private int position;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "column_permissions", joinColumns = {@JoinColumn(name = "column_id")},
               inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private List<Role> permittedRoles;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Team team;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "column", fetch = FetchType.EAGER)
    private List<Card> cards;

    public Optional<Card> getCardById(UUID cardId) {
        return cards.stream()
                    .filter(card -> card.getId()
                                        .compareTo(cardId) == 0)
                    .findFirst();
    }

    public String getType() {
        return switch (columnType) {
            case 0 -> "backlog";
            case 1 -> "inwork";
            case 2 -> "oncheck";
            case 3 -> "done";
            default -> "";
        };
    }

    public static Column copyColumn(Column column, Team team) {
        return Column.builder()
                     .position(column.getPosition())
                     .columnType(column.getColumnType())
                     .team(team)
                     .name(column.getName())
                     .id(UUID.randomUUID())
                     .build();
    }

}
