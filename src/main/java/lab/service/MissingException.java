package lab.service;

public class MissingException extends RuntimeException {
    public MissingException(String message) {
        super(message);
    }
}
