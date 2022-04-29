package com.kendar.finance.data;

import com.kendar.finance.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsersRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE upper(u.name) = upper(?1) and  upper(u.password) = upper(?2)")
    User findByLoginPassword(String user, String password);
}
