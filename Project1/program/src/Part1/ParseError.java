package Part1;
public class ParseError extends Exception {
    public String getMessage() {
        return "parse error";
    }
}