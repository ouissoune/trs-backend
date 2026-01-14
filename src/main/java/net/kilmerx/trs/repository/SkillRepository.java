package net.kilmerx.trs.repository;

import net.kilmerx.trs.model.Skill;
import net.kilmerx.trs.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByTeacher(Teacher teacher);

    List<Skill> findByTeacherId(Long teacherId);
}
