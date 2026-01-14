package net.kilmerx.trs.repository;

import net.kilmerx.trs.model.Admin;
import net.kilmerx.trs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser(User user);

    Optional<Admin> findByUserId(Long userId);
}
