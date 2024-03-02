package jeff.core.exception;

import jeff.common.exception.MyException;

/**
 * 所有查不到資料的情況，都用這個例外。
 */
public class MyNotFoundException extends MyException {

    public MyNotFoundException(String message) {
        super(message);
    }

    public MyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
