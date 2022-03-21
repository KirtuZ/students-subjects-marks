package telran.students.jpa.entities;

import telran.students.dto.Subject;

import javax.persistence.*;

@Entity
@Table(name = "subjects")
public class SubjectJpa {
    @Id
    int id;
    @Column(nullable = false, unique = true)
    String subject;

    public static SubjectJpa build(Subject subject) {
        SubjectJpa subjectJpa = new SubjectJpa();
        subjectJpa.subject = subject.subject;
        subjectJpa.id = subject.id;

        return subjectJpa;
    }

    public Subject getSubjectDto() {
        Subject subject = new Subject();
        subject.id = this.id;
        subject.subject = this.subject;

        return subject;
    }
}
