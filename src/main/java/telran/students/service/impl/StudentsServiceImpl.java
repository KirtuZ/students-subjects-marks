package telran.students.service.impl;

import com.mongodb.internal.operation.AggregateOperation;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telran.students.dto.*;
import telran.students.entities.*;
import telran.students.repository.*;
import telran.students.service.interfaces.*;

import java.util.*;

@Service
public class StudentsServiceImpl implements StudentsService {
    StudentsRepository studentsRepository;
    SubjectRepository subjectRepository;
    MongoTemplate mongoTemplate;

    public StudentsServiceImpl(StudentsRepository studentsRepository,
                               SubjectRepository subjectRepository,
                               MongoTemplate mongoTemplate) {
        this.studentsRepository = studentsRepository;
        this.subjectRepository = subjectRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void addStudent(Student student) {
        studentsRepository.insert(new StudentDoc(student.id, student.name));
    }

    @Override
    public void addSubject(Subject subject) {
        subjectRepository.insert(new SubjectDoc(subject.id, subject.subject));
    }

    @Override
    @Transactional
    public Rating addMark(Rating mark) {
        StudentDoc studentDoc = studentsRepository.findById(mark.studentId).orElse(null);

        if (studentDoc == null) {
            return null;
        }

        SubjectDoc subjectDoc = subjectRepository.findById(mark.subjectId).orElse(null);

        if (subjectDoc == null) {
            return null;
        }

        studentDoc.getMarks().add(new SubjectMark(subjectDoc.getSubject(), mark.mark));
        studentsRepository.save(studentDoc);

        return mark;
    }

    @Override
    public List<StudentSubjectMark> getMarksStudentSubject(String name, String subject) {
        StudentDoc studentDoc = studentsRepository.findByName(name);

        if (studentDoc == null) {
            return Collections.emptyList();
        }

        return studentDoc.getMarks().stream()
                .filter(doc -> doc.getSubject().equals(subject))
                .map(sm -> getStudentSubjectMark(sm, name))
                .toList();
    }

    private StudentSubjectMark getStudentSubjectMark(SubjectMark sm, String name) {
        return new StudentSubjectMark() {
            @Override
            public String getStudentName() {
                return name;
            }

            @Override
            public String getSubjectSubject() {
                return sm.getSubject();
            }

            @Override
            public int getMark() {
                return sm.getMark();
            }
        };
    }

    @Override
    public List<String> getBestStudents() {
        return null;
    }

    @Override
    public List<String> getTopStudents(int nStudents) {
        List<AggregationOperation> listOperations = getSortedStudentAvgMark();
        LimitOperation limit = Aggregation.limit(nStudents);
        listOperations.add(limit);

        List<Document> documentList = mongoTemplate.aggregate(
                Aggregation.newAggregation(listOperations),
                StudentDoc.class,
                Document.class
        ).getMappedResults();

        return documentList.stream().map(document -> document.getString("_id") + " | " + document.getDouble("avgMark")).toList();
    }

    private List<AggregationOperation> getSortedStudentAvgMark() {
        UnwindOperation unwindOperation = Aggregation.unwind("marks");
        GroupOperation groupOperation = Aggregation.group("name").avg("marks.mark").as("avgMark");
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "avgMark");

        return new ArrayList<>(Arrays.asList(unwindOperation, groupOperation, sortOperation));
    }

    @Override
    public List<Student> getTopBestStudentsSubject(int nStudents, String subject) {
        return null;
    }

    @Override
    public List<StudentSubjectMark> getMarksOfWorstStudents(int nStudents) {
        return null;
    }

    @Override
    public List<IntervalMarks> marksDistribution(int interval) {
        return null;
    }

    @Override
    public List<String> jpqlQuery(String jpql) {
        return null;
    }

    @Override
    public List<String> nativeQuery(String query) {
        System.out.println("native!");
        try {
            BasicQuery basicQuery = new BasicQuery(query);
            List<StudentDoc> docs = mongoTemplate.find(basicQuery, StudentDoc.class);

            return docs.stream().map(StudentDoc::toString).toList();
        } catch (Exception e) {
            e.printStackTrace();
            ArrayList<String> errorMessage = new ArrayList<>();
            errorMessage.add(e.getMessage());
            return errorMessage;
        }
    }

    @Override
    public List<Student> removeStudents(double avgMark, long nMarks) {
        return null;
    }
}