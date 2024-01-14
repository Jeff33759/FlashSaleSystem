package jeff.highconcurrency.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.highconcurrency.persistent.model.mongo.dao.ReactiveFlashSaleEventLogRepo;
import jeff.highconcurrency.persistent.model.mongo.po.ReactiveFlashSaleEventLog;
import jeff.highconcurrency.service.FlashSaleEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class FlashSaleEventServiceImpl implements FlashSaleEventService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReactiveFlashSaleEventLogRepo reactiveFlashSaleEventLogRepo;



    /**
     *
     * @return
     */
    @Override
    public Mono<ResponseObject> consumeFlashSaleEvent() {



        return reactiveFlashSaleEventLogRepo.selectByFseIdAndTransNumAndHasNotBeenConsumed(1, 1)
                .map(flashSaleEventLog -> new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.valueToTree(flashSaleEventLog), "success."));
    }
}
