package telran.students.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import telran.students.dto.Student;
import telran.students.jpa.entities.MarkJpa;
import telran.students.jpa.projections.StudentProjection;
import telran.students.service.interfaces.IntervalMarks;
import telran.students.service.interfaces.StudentSubjectMark;

import java.util.List;

public interface MarksRepository extends JpaRepository<MarkJpa, Integer> {
    List<StudentSubjectMark> findByStudentNameAndSubjectSubject(String name, String subject);

    @Query("select student.name, avg(mark) from MarkJpa group by student.name " +
            "having avg(mark) > (select avg(m2.mark) from MarkJpa m2) order by avg(mark) desc")
    List<String> findBestStudents();

    @Query(value = "select s.name, round(avg(m.mark)) from marks m " +
            "join students s on s.id = m.student_id " +
            "group by s.name order by avg(m.mark) desc " +
            "limit :limit", nativeQuery = true)
    List<String> getTopStudents(@Param("limit") int nStudents);

    @Query(value = "select s.* from marks m " +
            "join students s on s.id = m.student_id " +
            "join subjects su on su.id = m.subject_id " +
            "where su.subject = :subject " +
            "group by s.id, s.name order by avg(m.mark) desc " +
            "limit :limit", nativeQuery = true)
    List<StudentProjection> getTopBestStudentsSubject(@Param("limit") int nStudents, @Param("subject") String subject);

    @Query(value = "select s.name as studentName, su.subject as subjectSubject, m.mark " +
            "from marks m " +
            "    join students s on s.id = m.student_id " +
            "    join subjects su on su.id = m.subject_id " +
            "where s.id in (" +
            "    select tmp.id from (" +
            "        select s2.id as id, avg(m2.mark) " +
            "        from marks m2 " +
            "            join students s2 on s2.id = m2.student_id " +
            "            group by s2.id " +
            "            order by avg(m2.mark) " +
            "            limit :limit " +
            "    ) as tmp) order by studentName", nativeQuery = true)
    List<StudentSubjectMark> getMarksOfWorstStudents(@Param("limit") int nStudents);

    @Query(value = "select mark / :interval * :interval as `min`, mark / :interval * :interval + (:interval - 1) as `max`, count(*) " +
            "from marks group by `min`, `max` order  by `min`", nativeQuery = true)
    List<IntervalMarks> marksDistribution(@Param("interval") int interval);
}
