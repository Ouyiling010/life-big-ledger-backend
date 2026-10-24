package com.life.ledger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 业务自定义配置 app.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cache cache = new Cache();
    private Ai ai = new Ai();
    private Upload upload = new Upload();

    @Data
    public static class Cache {
        /** local = 进程内缓存；redis = 使用 Redis */
        private String type = "local";
    }

    @Data
    public static class Ai {
        private boolean enabled = true;
        private String provider = "zhipu";
        private String apiKey = "";
        private String model = "glm-4-flash";
        private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        private int timeoutSeconds = 60;
    }

    @Data
    public static class Upload {
        private String dir = "./uploads";
    }
}
