package jeff.highconcurrency.util.redis.exception;

import jeff.common.exception.MyException;

/**
 * 響應式redis函式庫相關的例外。
 */
public class MyReactiveRedisException extends MyException {

    public MyReactiveRedisException(String message) {
        super(message);
    }

    public MyReactiveRedisException(String message, Throwable cause) {
        super(message, cause);
    }

}
