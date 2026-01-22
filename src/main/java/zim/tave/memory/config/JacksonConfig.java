package zim.tave.memory.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer utcOffsetDateTimeCustomizer() {
        return builder -> {
            // Ensure UTC baseline
            builder.timeZone(TimeZone.getTimeZone("UTC"));
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Force OffsetDateTime format globally (UTC -> "Z")
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
                @Override
                public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException {
                    if (value == null) {
                        gen.writeNull();
                        return;
                    }
                    gen.writeString(value.format(OFFSET_DATE_TIME_FORMATTER));
                }
            });
            javaTimeModule.addDeserializer(OffsetDateTime.class, new JsonDeserializer<>() {
                @Override
                public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String raw = p.getValueAsString();
                    if (raw == null || raw.isBlank()) {
                        return null;
                    }
                    return OffsetDateTime.parse(raw, OFFSET_DATE_TIME_FORMATTER);
                }
            });

            builder.modules(javaTimeModule);
        };
    }

    /**
     * Helper for normalizing an OffsetDateTime to UTC when needed.
     * (Not wired automatically; kept for consistency utilities if required.)
     */
    public static OffsetDateTime normalizeToUtc(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}

