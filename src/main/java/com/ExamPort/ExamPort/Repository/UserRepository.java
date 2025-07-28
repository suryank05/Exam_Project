package com.ExamPort.ExamPort.Repository;

import com.ExamPort.ExamPort.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    java.util.List<Object[]> countUsersByRole();
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
