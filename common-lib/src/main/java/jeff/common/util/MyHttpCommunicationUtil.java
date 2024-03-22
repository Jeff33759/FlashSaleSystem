package jeff.common.util;

import jeff.common.entity.dto.inner.InnerCommunicationDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 系統之間溝通用的一些共用方法。
 */
@Component
public class MyHttpCommunicationUtil {

    /**
     * 定義正常的Http狀態碼。
     * 本系統設計狀態碼200代表"溝通正常"，但是body裡面的{@link InnerCommunicationDto}的code可能不是200。
     */
    private final Set<HttpStatus> correctStatusSet = new HashSet<>(Arrays.asList(new HttpStatus[]{
            HttpStatus.OK
    }));

    /**
     * 檢查下游Server回應的Http狀態碼，是否符合預期。
     */
    public boolean isHttpStatusFromDownstreamCorrect(HttpStatus statusFromDownstream) {
        return correctStatusSet.contains(statusFromDownstream);
    }


}
