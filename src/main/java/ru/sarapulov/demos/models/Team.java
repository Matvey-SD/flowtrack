package ru.sarapulov.demos.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String team_name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "team", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TeamMember> members;

    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "team", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Column> columns;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "team", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Role> roles;

    public Role getDefaultRole() {
        return roles.stream()
                    .filter(Role::isDefaultRole)
                    .findFirst()
                    .orElseThrow();
    }

    public Role getOwnerRole() {
        return roles.stream()
                    .filter(Role::isOwner)
                    .findFirst()
                    .orElseThrow();
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "team", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Document> documents;

    public static Team createTeam(String teamName, TeamMember owner) {
        return builder().team_name(teamName)
                        .members(List.of(owner))
                        .build();
    }

    public List<Column> getSortedColumns() {
        return columns.stream()
                      .sorted(Comparator.comparingInt(Column::getPosition))
                      .toList();
    }

    public List<Card> getAllTeamCards() {
        return columns.stream()
                      .flatMap(column -> column.getCards()
                                               .stream())
                      .toList();

    }

    public Column getColumn(UUID id) {
        return columns.stream()
                      .filter(column -> column.getId()
                                              .compareTo(id) == 0)
                      .findFirst()
                      .orElseThrow();
    }

}





