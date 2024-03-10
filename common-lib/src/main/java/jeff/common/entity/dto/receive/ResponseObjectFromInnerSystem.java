package jeff.common.entity.dto.receive;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用來承接來自其他內部系統模組的回應，基本上同ResponseObject，但還是基於單一職責分開管理，以後複習也比較一目瞭然。
 */
@NoArgsConstructor //要有無參數建構子，SpringBoot才可以利用Jackson對此物件進行反序列化，否則會跳"InvalidDefinitionException"
@Getter //加上getter，SpringBoot才可以利用Jackson對此物件進行序列化，否則會跳HttpMessageNotWritableException
public class ResponseObjectFromInnerSystem {

    private int code;

    private JsonNode content;

    private String msg;

}
