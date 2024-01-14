package jeff.core.exception;

import jeff.common.exception.MyException;

/**
 * 訂單相關的例外。
 */
public class OrderException extends MyException {

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

}
