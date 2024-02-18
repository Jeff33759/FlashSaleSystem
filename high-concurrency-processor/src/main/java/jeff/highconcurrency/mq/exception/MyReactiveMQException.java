package jeff.highconcurrency.mq.exception;

import jeff.common.exception.MyException;

/**
 * 響應式MQ相關的例外。
 */
public class MyReactiveMQException extends MyException {

    public MyReactiveMQException(String message) {
        super(message);
    }

    public MyReactiveMQException(String message, Throwable cause) {
        super(message, cause);
    }

}
