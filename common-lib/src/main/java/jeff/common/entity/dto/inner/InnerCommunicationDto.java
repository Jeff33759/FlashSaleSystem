package jeff.common.entity.dto.inner;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.dto.outer.OuterCommunicationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用來承接/發送來自其他內部系統模組的回應，基本上同{@link OuterCommunicationDto}，但還是基於單一職責分開管理，以後複習也比較一目瞭然。
 */
@AllArgsConstructor
@NoArgsConstructor //要有無參數建構子，SpringBoot才可以利用Jackson對此物件進行反序列化，否則會跳"InvalidDefinitionException"
@Getter //加上getter，SpringBoot才可以利用Jackson對此物件進行序列化，否則會跳HttpMessageNotWritableException
public class InnerCommunicationDto {

    private int code;

    private JsonNode content;

    private String msg;

}
