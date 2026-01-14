package net.kilmerx.trs.repository;

import net.kilmerx.trs.model.Reservation;
import net.kilmerx.trs.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStudent(Student student);

    List<Reservation> findByStudentId(Long studentId);

    Optional<Reservation> findByStudentAndSlotId(Student student, Long slotId);
}
