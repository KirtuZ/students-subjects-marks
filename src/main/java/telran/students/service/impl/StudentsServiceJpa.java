package telran.students.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telran.students.dto.*;
import telran.students.jpa.entities.*;
import telran.students.jpa.repo.*;
import telran.students.service.interfaces.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class StudentsServiceJpa implements StudentsService {
    StudentsRepository studentsRepository;
    SubjectsRepository subjectsRepository;
    MarksRepository marksRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public StudentsServiceJpa(StudentsRepository studentsRepository, SubjectsRepository subjectsRepository,
                              MarksRepository marksRepository) {
        this.studentsRepository = studentsRepository;
        this.subjectsRepository = subjectsRepository;
        this.marksRepository = marksRepository;
    }

    @Override
    public void addStudent(Student student) {
        studentsRepository.save(StudentJpa.build(student));
    }

    @Override
    public void addSubject(Subject subject) {
        subjectsRepository.save(SubjectJpa.build(subject));
    }

    @Override
    @Transactional
    public Rating addMark(Rating mark) {
        StudentJpa studentJpa = studentsRepository.findById(mark.studentId).orElse(null);
        SubjectJpa subjectJpa = subjectsRepository.findById(mark.subjectId).orElse(null);

        if (studentJpa != null && subjectJpa != null) {
            MarkJpa markJpa = new MarkJpa(studentJpa, subjectJpa, mark.mark);
            marksRepository.save(markJpa);

            return mark;
        }

        return null;
    }

    @Override
    public List<StudentSubjectMark> getMarksStudentSubject(String name, String subject) {
        return marksRepository.findByStudentNameAndSubjectSubject(name, subject);
    }

    @Override
    public List<String> getBestStudents() {
        return marksRepository.findBestStudents();
    }

    @Override
    public List<String> getTopStudents(int nStudents) {
        return marksRepository.getTopStudents(nStudents);
    }

    @Override
    public List<Student> getTopBestStudentsSubject(int nStudents, String subject) {
        return marksRepository.getTopBestStudentsSubject(nStudents, subject).stream()
                .map(e -> new Student(e.getId(), e.getName())).toList();
    }

    @Override
    public List<StudentSubjectMark> getMarksOfWorstStudents(int nStudents) {
        return marksRepository.getMarksOfWorstStudents(nStudents);
    }

    @Override
    public List<IntervalMarks> marksDistribution(int interval) {
        return marksRepository.marksDistribution(interval);
    }

    @Override
    public List<String> jpqlQuery(String jpql) {
        Query query = entityManager.createQuery(jpql);
        List res = query.getResultList();

        if (res.isEmpty()) {
            return Collections.emptyList();
        }

        return res.get(0).getClass().isArray() ? multiProjectionRequest(res) : simpleRequest(res);
    }

    @Override
    public List<String> nativeQuery(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        List res = query.getResultList();

        return res.get(0).getClass().isArray() ? multiProjectionRequest(res) : simpleRequest(res);
    }

    @Override
    @Transactional
    public List<Student> removeStudents(double avgMark, long nMarks) {
        List<StudentJpa> listJpa = studentsRepository.findStudentsForDeletions(avgMark, nMarks);
        studentsRepository.deleteAll(listJpa);
//        studentsRepository.deleteStudents(avgMark, nMarks);
        return listJpa.stream().map(StudentJpa::getStudentDto).toList();
    }

    private List<String> multiProjectionRequest(List<Object[]> res) {
        return res.stream().map(Arrays::deepToString).toList();
    }

    private List<String> simpleRequest(List<Object> res) {
        return res.stream().map(Object::toString).toList();
    }


}
