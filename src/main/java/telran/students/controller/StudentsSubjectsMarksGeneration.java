package telran.students.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import telran.students.service.interfaces.StudentsService;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import telran.students.dto.*;

//@Component
public class StudentsSubjectsMarksGeneration {
    static Logger LOG = LoggerFactory.getLogger("generation");
    @Value("${app.generation.amount: 100}")
    int nMarks;
    @Autowired
    StudentsService studentsService;
    String names[] = {"Abraham", "Sarah", "Itshak", "Rahel", "Asaf", "Iacob","Rivka", "Yosef",
            "Benyamin", "Dan", "Ruben", "Moshe", "Aron", "Yehashua", "David", "Salomon", "Nefertiti",
            "Naftaly", "Natan","Asher"};
    String subjects[] = {"Java core", "Java Technologies",
            "Spring Data", "Spring Security", "Spring Cloud", "CSS", "HTML", "JS", "React", "Material-UI"};


    @PostConstruct
    void createDB() {
        addStudents();
        addSubjects();
        addMarks();

        LOG.info("created {} marks in DB", nMarks);
    }

    private int getRandomNum(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void addMarks() {
        IntStream.range(0, nMarks).forEach(i -> {
            addMark();
        });
    }

    private void addMark() {
        int studentId = getRandomNum(1, names.length);
        int subjectId = getRandomNum(1, subjects.length);
        int mark = getRandomNum(60, 100);

        studentsService.addMark(new Rating(studentId, subjectId, mark));
    }

    private void addSubjects() {
        IntStream.range(0, subjects.length).forEach(i -> {
            studentsService.addSubject(new Subject(i + 1, subjects[i]));
        });
    }

    private void addStudents() {
        IntStream.range(0, names.length).forEach(i -> {
            studentsService.addStudent(new Student(i + 1, names[i]));
        });
    }
}
