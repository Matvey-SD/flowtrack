package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.Role;

import java.util.UUID;

public interface RolesRepository extends JpaRepository<Role, UUID> {}
