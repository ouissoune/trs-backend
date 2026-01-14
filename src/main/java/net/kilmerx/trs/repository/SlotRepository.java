package net.kilmerx.trs.repository;

import net.kilmerx.trs.model.Slot;
import net.kilmerx.trs.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByTeacher(Teacher teacher);

    List<Slot> findByTeacherId(Long teacherId);

    List<Slot> findByTeacherAndAvailable(Teacher teacher, Boolean available);

    List<Slot> findByStartDateTimeAfterAndStartDateTimeBeforeAndTeacherId(LocalDateTime start, LocalDateTime end,
            Long teacherId);
}
