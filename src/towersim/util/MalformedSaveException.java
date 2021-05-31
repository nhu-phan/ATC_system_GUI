package towersim.util;

/**
 * An exception that is thrown when a save file is in invalid format or contain incorrect data
 */
public class MalformedSaveException extends Exception {
    /**
     * Constructs a new MalformedSaveException with no detail message or cause
     */
    public MalformedSaveException() {
        super();
    }

    /**
     * Constructs a new MalformedSaveException that contains a helpful detail
     * message explaining why exception occurred
     * @param message explaining why exception occurred
     */
    public MalformedSaveException(String message) {
        super(message);
    }


    /**
     * Constructs a new MalformedSaveException that stores the underlying cause of exception
     * @param cause - throwable that caused the exception
     */
    public MalformedSaveException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MalformedSaveException that contains a helpful detail
     *      * message explaining why exception occurred and the underlying cause of exception
     * @param message explaining why exception occurred
     * @param cause - throwable that caused the exception
     */
    public MalformedSaveException(String message, Throwable cause) {

        super(message, cause);
    }
}
