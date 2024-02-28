package jeff.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.persistent.model.mysql.dao.SaleEventDAO;
import jeff.persistent.model.mysql.po.SaleEvent;
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
    ObjectMapper objectMapper;

    /**
     * 修改一般銷售案件的狀態(上架中/已下架)
     * 採用軟刪除，所以下架的情況，只將MySQL的狀態欄位改為已下架。
     *
     * @param {"se_id":1,"is_public":true}
     */
    public ResponseObject updateStateOfNormalSaleEvent(JsonNode param, MyRequestContext context) {
        saleEventDAO.updateStateById(param.get("se_id").asInt(), param.get("is_public").asBoolean());

        return new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.createObjectNode(), "The state of sale-event updated successfully.");
    }

}
