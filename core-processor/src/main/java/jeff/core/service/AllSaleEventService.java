package jeff.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.persistent.model.mongo.dao.FlashSaleEventLogRepo;
import jeff.persistent.model.mysql.dao.FlashSaleEventDAO;
import jeff.persistent.model.mysql.dao.SaleEventDAO;
import jeff.persistent.model.mysql.po.SaleEvent;
import jeff.redis.util.MyRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 所有類型的銷售案件相關的服務器，包括一般銷售案件和快閃銷售案件。
 */
@Slf4j
@Service
public class AllSaleEventService {

    @Autowired
    private SaleEventDAO saleEventDAO;

    @Autowired
    private FlashSaleEventDAO flashSaleEventDAO;

    @Autowired
    FlashSaleEventLogRepo flashSaleEventLogRepo;

    @Autowired
    private MyRedisUtil myRedisUtil;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 將某個一般銷售案件的狀態更改為(上架中/已下架)。
     *
     * 採用軟刪除，所以下架的情況，只將MySQL的狀態欄位改為已下架。
     *
     * @param {"se_id":1,"is_public":false}
     */
    public ResponseObject updateStateOfNormalSaleEvent(JsonNode param, MyRequestContext context) {
        saleEventDAO.updateStateById(param.get("se_id").asInt(), param.get("is_public").asBoolean());

        return new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.createObjectNode(), "The state of sale-event updated successfully.");
    }

    /**
     * 將某個快閃銷售案件的狀態更改為已下架。
     *
     * 1.採用軟刪除，所以下架的情況，只將MySQL的狀態欄位改為已下架。
     * 2.利用排程掃進redis queue的資料，要清掉
     * 3.mongo裡面，is_consumed=false的資料要清掉(節省空間)，但是is_consumed=true的別刪，因為mySQL裡面的order有存fse_id，可以隨時找回mongo看看快閃銷售案件的一些紀錄。
     *
     * @param {"fse_id":1}
     */
    public ResponseObject closeFlashSaleEvent(JsonNode param, MyRequestContext context) {
        int fseId = param.get("fse_id").asInt();

//      1、更改MySQL的狀態為已下架
        flashSaleEventDAO.updateStateById(fseId, false);

//      2、清掉利用排程快取進redis queue的資料
        myRedisUtil.removeKey("fse_" + fseId);

//      3、清掉mongo裡面is_consumed=false的資料
        flashSaleEventLogRepo.deleteByFseIdAndIsConsumed(fseId, false);

        return new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.createObjectNode(), "The flash-sale-event removed successfully.");
    }

}
