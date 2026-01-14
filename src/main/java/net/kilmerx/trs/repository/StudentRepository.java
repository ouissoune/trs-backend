package net.kilmerx.trs.repository;

import net.kilmerx.trs.model.Student;
import net.kilmerx.trs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);

    Optional<Student> findByUserId(Long userId);
}
