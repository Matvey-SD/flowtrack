package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.CardDocument;

import java.util.UUID;

public interface CardDocumentsRepository extends JpaRepository<CardDocument, UUID> {}
