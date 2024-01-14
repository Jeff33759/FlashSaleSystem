package jeff.highconcurrency.service;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.highconcurrency.persistent.model.mongo.po.ReactiveFlashSaleEventLog;
import reactor.core.publisher.Mono;

/**
 * 快閃銷售案件相關的業務邏輯。
 */
public interface FlashSaleEventService {

    /**
     * 消費一個快閃銷售案件，例如使用者搶購一張演唱會門票等等...
     */
    Mono<ResponseObject> consumeFlashSaleEvent();

}