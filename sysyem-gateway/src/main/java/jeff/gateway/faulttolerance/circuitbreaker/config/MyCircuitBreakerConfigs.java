package jeff.gateway.faulttolerance.circuitbreaker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import jeff.gateway.faulttolerance.circuitbreaker.prop.MyCBPropertiesForHCP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerResilience4JFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * 斷路器的註冊與配置。
 *
 * 參考文件:
 * https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/#spring-cloud-circuitbreaker-filter-factory
 * https://cloud.spring.io/spring-cloud-circuitbreaker/reference/html/spring-cloud-circuitbreaker.html
 * https://resilience4j.readme.io/docs/examples
 *
 * 參考類別:
 * {@link SpringCloudCircuitBreakerFilterFactory}、{@link SpringCloudCircuitBreakerResilience4JFilterFactory}
 */
@Configuration
public class MyCircuitBreakerConfigs {

    @Autowired
    private MyCBPropertiesForHCP myCBPropertiesForHighCurrencyProcessor;


    /**
     * 客製化斷路器的配置。
     * 目前gateway找不到純配置的做法，似乎只能用java-config的方式去做。
     * 所有訪問HighConcurrencyProcessor的router都是吃這個斷路器實例，在spring.cloud.gateway.routers.filters那裡指定
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> customizerCBForHighConcurrencyProcessor() {

//      設置斷路器參數
        CircuitBreakerConfig myCBConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(myCBPropertiesForHighCurrencyProcessor.getFailureRateThreshold()) //故障率閥值，預設50(%)。當總請求數到達minimumNumberOfCalls，且錯誤請求占比>=此閥值，則打開斷路器
                .slowCallRateThreshold(myCBPropertiesForHighCurrencyProcessor.getSlowCallRateThreshold()) //慢調用閥值，預設100(%)。當調用執行時間大於slowCallDurationThreshold則視為慢調用。當慢調用的百分比>=此閥值，則打開斷路器
                .slowCallDurationThreshold(myCBPropertiesForHighCurrencyProcessor.getSlowCallDurationThreshold()) //多長時間視為慢調用。有些router是沒設CB的，所以不能只有設這個而沒設HttpClientResponseTimeout
                .minimumNumberOfCalls(myCBPropertiesForHighCurrencyProcessor.getMinimumNumberOfCalls()) //觸發"計算錯誤率或者慢調用"所必須滿足的調用數，預設100。如果調用數<此設置，則即使錯誤或慢調用占比100%也不會打開斷路器(畢竟根本沒觸發計算)
                .slidingWindowSize(myCBPropertiesForHighCurrencyProcessor.getSlidingWindowSize()) //配置當斷路器關閉時，記錄呼叫結果時所使用的滑動視窗的大小。根據窗口類型，單位可能是秒或者調用次數，預設100。可以簡單理解為，當觸發計算時，要抓取幾筆資料來計算? 這裡就是在設置那個範圍。例如類型為COUNT_BASED，大小100，那就是每當被觸發計算，都會抓過去最近的100個請求來計算占比，決定斷路器是否開啟。
                .slidingWindowType(myCBPropertiesForHighCurrencyProcessor.getSlidingWindowType()) //滑動窗口類型，預設COUNT_BASED。
                .permittedNumberOfCallsInHalfOpenState(myCBPropertiesForHighCurrencyProcessor.getPermittedNumberOfCallsInHalfOpenState()) //半開狀態允許的最大請求數，預設10。在半開狀態下，斷路器將允許最多XX個請求通過去執行業務邏輯，超過則直接fallback。如果XX個請求中，錯誤占比又超過閥值，則斷路器將重新進入開啟狀態。
                .waitDurationInOpenState(myCBPropertiesForHighCurrencyProcessor.getWaitDurationInOpenState()) //開啟斷路的保持時間。當斷路器開啟斷路，會等待此時間過去後，轉為半開狀態。預設60000(ms)
//                .maxWaitDurationInHalfOpenState() //半開狀態的保持時間，預設為0，就是不限時間。只有當接收到permittedNumberOfCallsInHalfOpenState個請求，再依照錯誤占比去切換斷路器狀態，不然會一直維持半開。
                .automaticTransitionFromOpenToHalfOpenEnabled(myCBPropertiesForHighCurrencyProcessor.isAutomaticTransitionFromOpenToHalfOpenEnabled()) //是否啟用"自動從開啟狀態過渡到半開狀態"，預設值為false。如果啟用，斷路器將在時間到後，自動從開啟狀態過渡到半開狀態，缺點是為了實現自動的這件事，會在程式啟動時多建立一個執行緒去監聽各斷路器實例；否則，需要手動呼叫斷路器的transitionToHalfOpen方法。
                .recordExceptions(myCBPropertiesForHighCurrencyProcessor.getRecordExceptions()) //異常列表，只有指定的異常和其子類，才會認為調用失敗；其它異常會被認為是調用成功，除非指定了ignoreExceptions
                .build();

//      設置執行超時(超過這時間，就中斷操作，拋TimeoutException。和HttpClient的responseTimeout不同的是，斷路器的執行時間，是方法級別，跟HttpClient是兩回事，所以若方法內部的DAO阻塞，那這個超時也可以作用)
        TimeLimiterConfig myTLConfig = TimeLimiterConfig.custom()
                .timeoutDuration(myCBPropertiesForHighCurrencyProcessor.getTimeoutDuration()) //這個框架預設是1秒。因為gateway都是純轉發，所以就抓httpClient的超時。
                .build();

        return reactiveR4JCBFactory -> reactiveR4JCBFactory.configure(
                resilience4JConfigBuilder -> resilience4JConfigBuilder
                        .circuitBreakerConfig(myCBConfig)
                        .timeLimiterConfig(myTLConfig)
                        .build(), "high-concurrency-processor-cb" //配置一個名為high-concurrency-processor-cb的斷路器實例
        );
    }


}
