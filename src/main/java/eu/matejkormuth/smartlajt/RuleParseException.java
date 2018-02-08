package eu.matejkormuth.smartlajt;

public class RuleParseException extends RuntimeException {

    public RuleParseException() {
    }

    public RuleParseException(String message) {
        super(message);
    }

    public RuleParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
