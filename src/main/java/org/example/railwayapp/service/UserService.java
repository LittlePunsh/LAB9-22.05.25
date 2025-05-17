package org.example.railwayapp.service;

import org.example.railwayapp.model.users.User;
import org.example.railwayapp.repository.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Метод для поиска пользователя по имени (только не удаленных)
    @Transactional("usersTransactionManager")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username);
    }

    // Метод для поиска пользователя по ID (может найти удаленных)
    @Transactional("usersTransactionManager")
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }


    @Transactional("usersTransactionManager")
    public void registerUser(String username, String rawPassword, String email) throws Exception {
        // Проверяем уникальность только среди НЕ удаленных пользователей
        if (userRepository.existsByUsernameAndDeletedFalse(username)) {
            throw new Exception("Username already exists: " + username);
        }
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new Exception("Email already exists: " + email);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Создаем нового пользователя (роль по умолчанию - "user", deleted = false по умолчанию)
        User newUser = new User(username, encodedPassword, email, "user");

        userRepository.save(newUser);
    }

    // Найти всех НЕ удаленных пользователей
    @Transactional("usersTransactionManager")
    public List<User> findAllNonDeletedUsers() {
        return userRepository.findAllByDeletedFalse();
    }

    // Сохранить или обновить пользователя (пароль не меняется через этот метод)
    @Transactional("usersTransactionManager")
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Метод для удаления пользователя, не из MYSQL
    @Transactional("usersTransactionManager")
    public void softDeleteUser(Long userId) throws Exception {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeleted(true);
            userRepository.save(user);
        } else {
            throw new Exception("User not found with ID: " + userId);
        }
    }

    // Метод для восстановления пользователя
    @Transactional("usersTransactionManager")
    public void restoreUser(Long userId) throws Exception {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeleted(false);
            userRepository.save(user);
        } else {
            throw new Exception("User not found with ID: " + userId);
        }
    }
}