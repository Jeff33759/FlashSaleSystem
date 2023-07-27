package jeff.common.exception;

/**
 * 自己定義的例外的父類。
 */
public class MyException extends RuntimeException {

    public MyException(String message) {
        super(message);
    }

    public MyException(String message, Throwable cause) {
        super(message, cause);
    }

}
