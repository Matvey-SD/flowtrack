package ru.sarapulov.demos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sarapulov.demos.models.User;

public interface UsersRepository extends JpaRepository<User, String> {
    User findUserByLogin(String login);

    boolean existsUserByLogin(String login);
}
