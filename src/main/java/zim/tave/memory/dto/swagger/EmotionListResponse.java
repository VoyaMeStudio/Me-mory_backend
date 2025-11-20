package zim.tave.memory.dto.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import zim.tave.memory.domain.Emotion;

import java.util.List;

@Schema(name = "EmotionListResponse")
public class EmotionListResponse {
    public int code;
    public String message;
    public List<Emotion> data;
}

