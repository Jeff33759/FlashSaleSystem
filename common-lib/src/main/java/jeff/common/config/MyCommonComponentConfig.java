package jeff.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 一些最通用的元件的註冊。
 */
@Configuration
public class MyCommonComponentConfig {

    /**
     * Jackson的Json解析器，可以實現POJO與JSON字串的互轉。
     * 本身雖為執行緒安全，不過是阻塞IO，所以若併發量真的大到哭爸，解析JSON可能會發生阻塞問題。
     * 但一般而言，JSON的解析都不會是效能瓶頸，除非特殊場景，所以遇到再說。
     *
     * 解決效能瓶頸方案:
     * 也許可以用實例池的方式去做，就別做成單例了，如此一來既不會對GC造成太大負擔，也可以保證效能問題。
     * */
    @Bean
    public ObjectMapper objectMapperBean() {
        return new ObjectMapper();
    }

}
