package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.TeamMember;

import java.util.UUID;

public interface TeamMembersRepository extends JpaRepository<TeamMember, UUID> {}
