package zim.tave.memory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "memory.file-upload")
public class FileUploadProperties {

    /**
     * Maximum file size in bytes.
     */
    private long maxFileSize;

    /**
     * Allowed MIME types for image uploads.
     */
    private List<String> allowedImageTypes = new ArrayList<>();

    /**
     * Allowed file extensions (lowercase, including dot) for image uploads.
     */
    private List<String> allowedImageExtensions = new ArrayList<>();
}

