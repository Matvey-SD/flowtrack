package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.Column;

import java.util.UUID;

public interface ColumnsRepository extends JpaRepository<Column, UUID> {}
