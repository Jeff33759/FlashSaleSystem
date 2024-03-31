package jeff.gateway.faulttolerance.circuitbreaker.prop;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * 讀取yml中的配置。
 * 專門給訪問HighCurrencyProcessor用的斷路器配置。
 */
@Configuration
@ConfigurationProperties(prefix = "my.circuitbreaker.configs.high-concurrency-processor-cb-config")
@Setter //要Setter，不然@ConfigurationProperties沒辦法把配置檔抓到的值賦值進member。
public class MyCBPropertiesForHCP {

    private int failureRateThreshold;

    private int slowCallRateThreshold;

    private Duration slowCallDurationThreshold;

    private int minimumNumberOfCalls;

    private int slidingWindowSize;

    private CircuitBreakerConfig.SlidingWindowType slidingWindowType;

    private int permittedNumberOfCallsInHalfOpenState;

    private Duration waitDurationInOpenState;

    private boolean automaticTransitionFromOpenToHalfOpenEnabled;

    private List<Class<? extends Throwable>> recordExceptions;

    private Duration timeoutDuration;

    public int getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public int getSlowCallRateThreshold() {
        return slowCallRateThreshold;
    }

    public Duration getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public CircuitBreakerConfig.SlidingWindowType getSlidingWindowType() {
        return slidingWindowType;
    }

    public int getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }

    public Duration getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

    public boolean isAutomaticTransitionFromOpenToHalfOpenEnabled() {
        return automaticTransitionFromOpenToHalfOpenEnabled;
    }

    public Class<? extends Throwable>[] getRecordExceptions() {
        return recordExceptions.toArray(new Class[recordExceptions.size()]);
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }
}
