package ru.sarapulov.demos.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team_members", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_login", "team_id"})})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Nonnull
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Team team;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private Role role;

    @OneToMany(mappedBy = "sender")
    private List<Comment> comments;

    public static TeamMember createOwner(User user) {
        return builder().role(Role.createOwnerRole())
                        .user(user)
                        .build();
    }

    public static TeamMember copyCreateOwner(User user, Role role) {
        return builder().role(Role.copyRole(role))
                        .user(user)
                        .build();
    }

}
