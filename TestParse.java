import java.util.regex.*;
public class TestParse {
    public static void main(String[] args) {
        String json = "course_name\": \"Machine Learning\",;
        Pattern p = Pattern.compile("\"course_name\"\\s*:\\s*");
        Matcher m = p.matcher(json);
        System.out.println(m.find());
    }
}
