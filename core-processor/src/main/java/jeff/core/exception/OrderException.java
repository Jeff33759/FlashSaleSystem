package jeff.core.exception;

/**
 * 訂單相關的例外。
 */
public class OrderException extends RuntimeException{

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message,cause);
    }

}
