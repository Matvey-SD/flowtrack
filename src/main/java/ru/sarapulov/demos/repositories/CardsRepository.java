package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sarapulov.demos.models.Card;

import java.util.UUID;

public interface CardsRepository extends JpaRepository<Card, UUID> {}
