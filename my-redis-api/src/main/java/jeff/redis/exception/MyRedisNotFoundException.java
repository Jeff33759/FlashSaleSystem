package jeff.redis.exception;

/**
 * 自己做的例外，當Redis找不到某個Key，就跳此例外。
 * */
public class MyRedisNotFoundException extends RuntimeException{

    public MyRedisNotFoundException(String message) {
        super(message);
    }

}
