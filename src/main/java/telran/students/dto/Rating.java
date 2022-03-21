package telran.students.dto;

public class Rating {
    public int studentId;
    public int subjectId;
    public int mark;

    public Rating() {

    }

    public Rating(int studentId, int subjectId, int mark) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.mark = mark;
    }
}
