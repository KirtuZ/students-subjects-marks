package telran.students.jpa.entities;

import javax.persistence.*;

@Entity
@Table(name = "marks")
public class MarkJpa {
    @Id
    @GeneratedValue
    int id;

    @ManyToOne
    StudentJpa student;

    @ManyToOne
    SubjectJpa subject;

    int mark;

    public MarkJpa() {

    }

    public MarkJpa(StudentJpa student, SubjectJpa subject, int mark) {
        this.student = student;
        this.subject = subject;
        this.mark = mark;
    }
}
