package telran.students.entities;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class SubjectMark {
    String subject;
    int mark;

    @Override
    public String toString() {
        return "SubjectMark{" +
                "subject='" + subject + '\'' +
                ", mark=" + mark +
                '}';
    }
}
