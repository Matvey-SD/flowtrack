package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.Document;

import java.util.UUID;

public interface DocumentsRepository extends JpaRepository<Document, UUID> {}
