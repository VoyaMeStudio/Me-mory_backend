package zim.tave.memory.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "memory")
@Getter
@Setter
public class MemoryProperties {
    private String baseUrl;
}
