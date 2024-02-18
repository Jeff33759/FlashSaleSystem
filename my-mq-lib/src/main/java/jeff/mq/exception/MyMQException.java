package jeff.mq.exception;

import jeff.common.exception.MyException;

/**
 * 當遭遇MQ相關失敗時噴錯。
 */
public class MyMQException extends MyException {

    public MyMQException(String message) {
        super(message);
    }

    public MyMQException(String message, Throwable cause) {
        super(message, cause);
    }

}
