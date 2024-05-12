package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.Team;

import java.util.UUID;

public interface TeamsRepository extends JpaRepository<Team, UUID> {
    public Team findTeamById(UUID id);
}
