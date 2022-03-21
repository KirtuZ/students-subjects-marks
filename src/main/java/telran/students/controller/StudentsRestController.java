package telran.students.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import telran.students.dto.QueryDto;
import telran.students.dto.QueryType;
import telran.students.dto.Student;
import telran.students.service.interfaces.*;

import java.util.*;

@RestController
@RequestMapping("/students")
public class StudentsRestController {
    StudentsService service;

    @Autowired
    public void setService(StudentsService service) {
        this.service = service;
    }

    @GetMapping("/subject/mark")
    public List<StudentSubjectMark> getStudentSubjectMarks(@RequestParam String name, @RequestParam String subject) {
        return service.getMarksStudentSubject(name, subject);
    }

    @GetMapping("/best")
    public List<String> getTopStudents(
            @RequestParam(required = false, defaultValue = "0", name = "amount") int nStudents) {
        return nStudents == 0 ? service.getBestStudents() : service.getTopStudents(nStudents);
    }

    @PostMapping("/query")
    public List<String> getQueryResult(@RequestBody QueryDto queryDto) {
        try {
            return queryDto.type == QueryType.JPQL ? service.jpqlQuery(queryDto.query) : service.nativeQuery(queryDto.query);
        } catch (Exception e) {
            List<String> response = new LinkedList<>();
            response.add("Wrong query");
            return response;
        }
    }

    @GetMapping("/top/subject")
    public List<Student> getTopBestStudentsSubject(@RequestParam int nStudents, @RequestParam String subject) {
        return service.getTopBestStudentsSubject(nStudents, subject);
    }

    @GetMapping("/worst")
    public List<StudentSubjectMark> getMarksOfWorstStudents(@RequestParam int nStudents) {
        return service.getMarksOfWorstStudents(nStudents);
    }

    @GetMapping("/marks")
    public List<IntervalMarks> marksDistribution(@RequestParam int interval) {
        return service.marksDistribution(interval);
    }

    @DeleteMapping("/worst")
    public List<Student> deleteStudents(@RequestParam("avgMark") double avgMark, @RequestParam("nMarks") long nMarks) {
        return service.removeStudents(avgMark, nMarks);
    }
}
