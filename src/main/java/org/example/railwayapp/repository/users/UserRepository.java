package org.example.railwayapp.repository.users;

import org.example.railwayapp.model.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по имени, только если он НЕ удален
    Optional<User> findByUsernameAndDeletedFalse(String username);

    // Проверить существование по имени, только если он НЕ удален
    boolean existsByUsernameAndDeletedFalse(String username);

    // Проверить существование по email, только если он НЕ удален
    boolean existsByEmailAndDeletedFalse(String email);

    // Найти всех пользователей, которые НЕ удалены (для отображения в таблице)
    List<User> findAllByDeletedFalse();

    // findById - по умолчанию находит пользователя независимо от флага deleted.
    // Чтобы найти удаленного пользователя для восстановления, а то вдруг админ рукопоп

}