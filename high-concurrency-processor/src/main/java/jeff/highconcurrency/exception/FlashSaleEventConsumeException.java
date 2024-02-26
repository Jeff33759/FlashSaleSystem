package jeff.highconcurrency.exception;

import jeff.common.exception.MyException;

/**
 * 消費快閃銷售案件的例外。
 */
public class FlashSaleEventConsumeException extends MyException {

    public FlashSaleEventConsumeException(String message) {
        super(message);
    }

    public FlashSaleEventConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

}
