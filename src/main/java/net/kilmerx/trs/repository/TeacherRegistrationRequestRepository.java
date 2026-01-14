package net.kilmerx.trs.repository;

import net.kilmerx.trs.model.TeacherRegistrationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRegistrationRequestRepository extends JpaRepository<TeacherRegistrationRequestEntity, Long> {
    boolean existsByUsernameAndStatus(String username, TeacherRegistrationRequestEntity.RequestStatus status);

    List<TeacherRegistrationRequestEntity> findAllByStatus(TeacherRegistrationRequestEntity.RequestStatus status);
}
