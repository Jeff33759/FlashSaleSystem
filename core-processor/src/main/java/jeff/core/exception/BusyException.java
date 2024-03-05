package jeff.core.exception;

import jeff.common.exception.MyException;

/**
 * 伺服端太過忙碌所拋的例外。
 */
public class BusyException extends MyException {
    public BusyException(String message) {
        super(message);
    }

    public BusyException(String message, Throwable cause) {
        super(message, cause);
    }
}
