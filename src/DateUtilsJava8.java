import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtilsJava8 {

    private DateTimeFormatter dateTimeFormatter;

    public DateUtilsJava8(String pattern){
        dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    public LocalDateTime parse(String target){
        return  LocalDateTime.parse(target, dateTimeFormatter);
    }

    public String format(LocalDateTime target){
        return target.format(dateTimeFormatter);
    }
}
