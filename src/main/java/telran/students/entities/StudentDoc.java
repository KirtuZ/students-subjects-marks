package telran.students.entities;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

@Getter
@Document(collection = "students")
public class StudentDoc {
    @Id
    int id;
    String name;
    List<SubjectMark> marks;

    public StudentDoc(int id, String name) {
        this.id = id;
        this.name = name;
        marks = new LinkedList<>();
    }

    @Override
    public String toString() {
        return "StudentDoc{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", marks=" + marks +
                '}';
    }
}
