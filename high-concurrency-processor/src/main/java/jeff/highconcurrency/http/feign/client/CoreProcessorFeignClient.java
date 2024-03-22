package jeff.highconcurrency.http.feign.client;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.highconcurrency.http.feign.config.ReactiveFeignConfigForCoreProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

/**
 * 訪問core-processor集群的Feign客戶端
 */
@Component
@ReactiveFeignClient(
        name = "core-processor", //如果開啟負載均衡模式，那麼這個名字要和你要打過去的服務集群名稱一樣(或者說要跟該集群註冊進服務治理中心的名稱一樣)
        path = "/inner",
        configuration = ReactiveFeignConfigForCoreProcessor.class
)
public interface CoreProcessorFeignClient {

    @PostMapping(value = "/flash-sale-event/query")
    Mono<ResponseEntity<Mono<InnerCommunicationDto>>> getFlashSaleEventInfo(@RequestHeader(value = "myUUID") String myUUID, @RequestBody JsonNode param);

}
