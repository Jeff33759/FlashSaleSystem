package jeff.common.entity.dto.send;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 自訂義的回應物件。
 *
 * 用於最上游的Server(通常是Gateway)對外部回應用。
 */
@AllArgsConstructor
@Getter //加上getter，SpringBoot才可以利用Jackson對此物件進行序列化，否則會跳HttpMessageNotWritableException
public class ResponseObject implements Serializable {

    private int code;

    private JsonNode content;

    private String msg;

}
