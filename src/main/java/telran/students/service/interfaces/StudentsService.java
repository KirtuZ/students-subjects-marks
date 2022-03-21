package telran.students.service.interfaces;

import telran.students.dto.*;

import java.util.List;

public interface StudentsService {
	void addStudent(Student student);
	void addSubject(Subject subject);
	Rating addMark(Rating mark);

	/**
	 * @param name of student for filtering
	 * @param subject subject for filtering
	 * @return students marks by subject
	 */
	List<StudentSubjectMark> getMarksStudentSubject(String name, String subject);

	/**
	 * @return names of students having average mark
	 * greater than average mark of all students
	 */
	List<String> getBestStudents();

	/**
	 * @param nStudents count students for returning
	 * @return names of best students
	 */
	List<String> getTopStudents(int nStudents);

	/**
	 * @param nStudents count students for returning
	 * @param subject subject for filtering
	 * @return names of the best students filtering by subject
	 */
	List<Student> getTopBestStudentsSubject(int nStudents, String subject);

	/**
	 * @param nStudents count students for returning
	 * @return data about marks for worst students
	 */
	List<StudentSubjectMark> getMarksOfWorstStudents(int nStudents);

	/**
	 * @param interval marks interval
	 * @return count marks of given interval
	 */
	List<IntervalMarks> marksDistribution(int interval);

	List<String> jpqlQuery(String jpql);
	List<String> nativeQuery(String sql);
	List<Student> removeStudents(double avgMark, long nMarks);
}