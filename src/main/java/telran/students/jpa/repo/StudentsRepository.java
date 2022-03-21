package telran.students.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import telran.students.jpa.entities.StudentJpa;

import java.util.List;

public interface StudentsRepository extends JpaRepository<StudentJpa, Integer> {
    @Modifying
    @Transactional
    @Query("delete from StudentJpa where id in (" +
            "select student.id from MarkJpa " +
            "group by student.id " +
            "having avg(mark) < :avgMark and count(mark) < :nMarks" +
        ")")
    int deleteStudents(@Param("avgMark") double avgMark, @Param("nMarks") long nMarks);

    @Query(value = "select s from StudentJpa  s where s.id" +
            " in (select student.id from MarkJpa group by student.id" +
            " having avg(mark) < :avgMark and count(mark) < :nMarks)")
    List<StudentJpa> findStudentsForDeletions(@Param("avgMark") double avgMark, @Param("nMarks") long nMarks);
}
