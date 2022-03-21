package telran.students;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import telran.students.dto.Rating;
import telran.students.dto.Student;
import telran.students.dto.Subject;
import telran.students.service.interfaces.StudentSubjectMark;
import telran.students.service.interfaces.StudentsService;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentsSubjectsMarksApplicationTests {
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    MockMvc mockMvc;
    @Autowired
    StudentsService studentsService;

    @Test
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    @Order(1)
    void dbLoad() {
        studentsService.addStudent(new Student(1, "Moshe"));
        studentsService.addStudent(new Student(2, "Sara"));
        studentsService.addStudent(new Student(3, "Vasya"));
        studentsService.addStudent(new Student(4, "Olya"));

        studentsService.addSubject(new Subject(1, "React"));
        studentsService.addSubject(new Subject(2, "Java"));

        studentsService.addMark(new Rating(1, 1, 90));
        studentsService.addMark(new Rating(1, 2, 90));
        studentsService.addMark(new Rating(2, 1, 80));
        studentsService.addMark(new Rating(2, 2, 80));
        studentsService.addMark(new Rating(3, 2, 40));
        studentsService.addMark(new Rating(4, 2, 45));
    }

    @Test
    @Order(10)
    void worstStudents() throws Exception {
        String name = "Vasya";
        String subject = "Java";
        int mark = 40;

        testWorstMarks(name, subject, mark);
    }

    private void testWorstMarks(String name, String subject, int mark) throws Exception {
        String resJson = mockMvc.perform(MockMvcRequestBuilders.get("/students/worst?nStudents=1"))
                .andReturn().getResponse().getContentAsString();
        StudentSubjectMark[] subjectMarks = mapper.readValue(resJson, StSuMark[].class);

        assertEquals(1, subjectMarks.length);

        StudentSubjectMark loser = subjectMarks[0];
        assertEquals(subject, loser.getSubjectSubject());
        assertEquals(name, loser.getStudentName());
        assertEquals(mark, loser.getMark());
    }


    @Test
    @Order(11)
    void deleteStudents() throws Exception {
        String resJson = mockMvc.perform(MockMvcRequestBuilders.delete("/students/worst?avgMark=45&nMarks=2"))
                .andReturn().getResponse().getContentAsString();
        Student[] students = mapper.readValue(resJson, Student[].class);
        Student[] expected = {new Student(3, "Vasya")};
        assertArrayEquals(expected, students);
        testWorstMarks("Olya", "Java", 45);
    }

    @Test
    void bestTopStudents() throws  Exception {
        String resJSON = mockMvc.perform(MockMvcRequestBuilders.get("/students/best?amount=1"))
                .andReturn().getResponse().getContentAsString();
        String[] res = mapper.readValue(resJSON, String[].class);
        assertEquals(1, res.length);
        assertTrue(res[0].contains("Moshe"));
    }

    @Test
    void bestStudents() throws  Exception {
        String resJSON = mockMvc.perform(MockMvcRequestBuilders.get("/students/best"))
                .andReturn().getResponse().getContentAsString();
        String[] res = mapper.readValue(resJSON, String[].class);
        assertEquals(2, res.length);
        assertTrue(res[0].contains("Moshe"));
        assertTrue(res[1].contains("Sara"));
    }


}

class StSuMark implements StudentSubjectMark {
    public String subjectSubject;
    public String studentName;
    public int mark;

    @Override
    public String getStudentName() {
        return studentName;
    }

    @Override
    public String getSubjectSubject() {
        return subjectSubject;
    }

    @Override
    public int getMark() {
        return mark;
    }
}
