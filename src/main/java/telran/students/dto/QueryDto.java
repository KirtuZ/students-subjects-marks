package telran.students.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class QueryDto {
    @NotNull
    public QueryType type;
    @NotEmpty
    public String query;
}
