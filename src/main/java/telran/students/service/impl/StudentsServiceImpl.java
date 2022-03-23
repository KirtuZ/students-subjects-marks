package telran.students.service.impl;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telran.students.dto.*;
import telran.students.entities.*;
import telran.students.repository.*;
import telran.students.service.interfaces.*;

import java.util.*;

@Service
public class StudentsServiceImpl implements StudentsService {
    private static final int MAX_MARK = 100;
    private static final int MIN_MARK = 60;
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
        studentsRepository.save(new StudentDoc(student.id, student.name));
    }

    @Override
    public void addSubject(Subject subject) {
        subjectRepository.save(new SubjectDoc(subject.id, subject.subject));
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
        List<AggregationOperation> operationList = getSortedStudentAvgMark(Sort.Direction.DESC);
        double avgCollegeMark = getAvgCollegeMark();
        MatchOperation matchOperation = Aggregation.match(Criteria.where("avgMark").gt(avgCollegeMark));
        operationList.add(matchOperation);

        return resultProcessing(operationList, false);
    }

    private double getAvgCollegeMark() {
        UnwindOperation unwindOperation = Aggregation.unwind("marks");
        GroupOperation groupOperation = Aggregation.group().avg("marks.mark").as("avgMark");
        Aggregation pipeline = Aggregation.newAggregation(unwindOperation, groupOperation);

        Document document = mongoTemplate.aggregate(pipeline, StudentDoc.class, Document.class)
                .getUniqueMappedResult();

        return document == null ? 0 : document.getDouble("avgMark");
    }

    @Override
    public List<String> getTopStudents(int nStudents) {
        List<AggregationOperation> operationList = getSortedStudentAvgMark(Sort.Direction.DESC);
        LimitOperation limit = Aggregation.limit(nStudents);
        operationList.add(limit);

        return resultProcessing(operationList, false);
    }

    private List<String> resultProcessing(List<AggregationOperation> listOperations, boolean nameOnly) {
        try {
            List<Document> documentList = mongoTemplate.aggregate(
                    Aggregation.newAggregation(listOperations),
                    StudentDoc.class,
                    Document.class
            ).getMappedResults();

            return documentList.stream().map(document -> {
                if (nameOnly) {
                    return document.getString("_id");
                }

                return document.getString("_id") + " | " + document.getDouble("avgMark").intValue();
            }).toList();
        } catch (Exception e) {
            ArrayList<String> errorMessages = new ArrayList<>();
            errorMessages.add(e.getMessage());

            return errorMessages;
        }

    }

    private List<AggregationOperation> getSortedStudentAvgMark(Sort.Direction sortDirection) {
        UnwindOperation unwindOperation = Aggregation.unwind("marks");
        GroupOperation groupOperation = Aggregation.group("name").avg("marks.mark").as("avgMark");
        SortOperation sortOperation = Aggregation.sort(sortDirection, "avgMark");

        return new ArrayList<>(Arrays.asList(unwindOperation, groupOperation, sortOperation));
    }

    @Override
    public List<Student> getTopBestStudentsSubject(int nStudents, String subject) {
        UnwindOperation unwindOperation = Aggregation.unwind("marks");
        MatchOperation matchOperation = Aggregation.match(Criteria.where("marks.subject").is(subject));
        GroupOperation groupOperation = Aggregation.group("id", "name").avg("marks.mark").as("avgMark");
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "avgMark");
        LimitOperation limitOperation = Aggregation.limit(nStudents);

        List<AggregationOperation> pipeline = new ArrayList<>(
                Arrays.asList(unwindOperation, matchOperation, groupOperation, sortOperation, limitOperation)
        );

        List<Document> mappedResults = mongoTemplate
                .aggregate(Aggregation.newAggregation(pipeline), StudentDoc.class, Document.class)
                .getMappedResults();

        return mappedResults.stream().map(this::getStudent).toList();
    }

    private Student getStudent(Document doc) {
        Document studentDoc = (Document) doc.get("_id");
        return new Student(studentDoc.getInteger("id"), studentDoc.getString("name"));
    }

    @Override
    public List<StudentSubjectMark> getMarksOfWorstStudents(int nStudents) {
        List<AggregationOperation> operationList = getSortedStudentAvgMark(Sort.Direction.ASC);
        LimitOperation limit = Aggregation.limit(nStudents);
        operationList.add(limit);
        List<String> names = resultProcessing(operationList, true);


        List<StudentDoc> studentDocs = studentsRepository.findByNameIn(names);

        if (studentDocs == null) {
            return Collections.emptyList();
        }

        return studentDocs.stream()
                .flatMap(sd -> sd.getMarks().stream().map(sm -> getStudentSubjectMark(sm, sd.getName())))
                .toList();
    }

    @Override
    public List<IntervalMarks> marksDistribution(int interval) {
        int nIntervals = ( MAX_MARK - MIN_MARK) / interval;
        UnwindOperation unwindOperation = Aggregation.unwind("marks");
        BucketAutoOperation bucketOperation = Aggregation.bucketAuto("marks.mark", nIntervals);
        List<Document> bucketDocs = mongoTemplate.aggregate(Aggregation.newAggregation(unwindOperation,
                        bucketOperation), StudentDoc.class, Document.class).getMappedResults();

        return bucketDocs.stream().map(this::getIntervalMarks).toList();
    }

    private IntervalMarks getIntervalMarks(Document doc) {
        Document interval = (Document) doc.get("_id");
        return new IntervalMarks() {
            public int getOccurrences() {
                return doc.getInteger("count");
            }
            public int getMin() {
                return interval.getInteger("min");
            }
            public int getMax() {
                return interval.getInteger("max");
            }

        };
    }

    @Override
    @Transactional
    public List<Student> removeStudents(double avgMark, long nMarks) {
        UnwindOperation unwindOperation = Aggregation.unwind("marks");

        GroupOperation groupOperation = Aggregation.group("id", "name")
                .avg("marks.mark").as("avgMark").count().as("count");

        MatchOperation matchOperation = Aggregation.match(Criteria.where("avgMark").lt(avgMark).and("count").lt(nMarks));
        List<Document> documents = mongoTemplate.aggregate(
                Aggregation.newAggregation(unwindOperation, groupOperation, matchOperation), StudentDoc.class,
                Document.class).getMappedResults();
        List<Student> studentsForRemoving = documents.stream().map(this::getStudent).toList();
        studentsRepository.deleteAllById(studentsForRemoving.stream().map(s -> s.id).toList());

        return studentsForRemoving;
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
}
