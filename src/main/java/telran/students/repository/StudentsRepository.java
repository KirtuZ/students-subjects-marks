package telran.students.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import telran.students.entities.StudentDoc;

public interface StudentsRepository extends MongoRepository<StudentDoc, Integer> {

    StudentDoc findByName(String name);
}
