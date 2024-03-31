package jeff.redis.exception;

import jeff.common.exception.MyException;

/**
 * redis相關的例外。
 */
public class MyRedisException extends MyException {

    public MyRedisException(String message) {
        super(message);
    }

    public MyRedisException(String message, Throwable cause) {
        super(message, cause);
    }

}
