package telran.students.jpa.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import telran.students.dto.Student;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "students")
public class StudentJpa {
    @Id
    int id;
    @Column(nullable = false, unique = true)
    String name;
    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    List<MarkJpa> marks;

    public static StudentJpa build(Student student) {
        StudentJpa studentJpa = new StudentJpa();
        studentJpa.name = student.name;
        studentJpa.id = student.id;

        return studentJpa;
    }

    public Student getStudentDto() {
        Student student = new Student();
        student.id = this.id;
        student.name = this.name;

        return student;
    }
}
