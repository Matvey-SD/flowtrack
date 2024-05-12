package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.entities.UserChangeDTO;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.UsersRepository;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private UsersRepository usersRepository;

    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return usersRepository.findUserByLogin(login);
    }

    public User updateUser(User user) {
        return usersRepository.findUserByLogin(user.getLogin());
    }

    public boolean tryRegisterUser(User user) {
        boolean userAlreadyExist = usersRepository.existsUserByLogin(user.getLogin());
        if (!userAlreadyExist) {
            addUser(user);
            return true;
        }
        return false;
    }

    public void addUser(User user) {
        user.setHashPassword(passwordEncoder.encode(user.getDecodedPassword()));
        usersRepository.save(user);
    }

    public void saveUserWithChanges(User user, UserChangeDTO userChanges) {
        if (user.getMail() == null || !user.getMail()
                                           .equals(userChanges.getMail())) {
            user.setMail(userChanges.getMail());
        }

        if (user.getFullName() == null || !user.getFullName()
                                           .equals(userChanges.getFullName())) {
            user.setFullName(userChanges.getFullName());
        }

        usersRepository.save(user);
    }

}
