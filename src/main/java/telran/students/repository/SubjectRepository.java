package telran.students.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import telran.students.entities.SubjectDoc;

public interface SubjectRepository extends MongoRepository<SubjectDoc, Integer> {
}
