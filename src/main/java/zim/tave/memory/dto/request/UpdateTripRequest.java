package zim.tave.memory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTripRequest {

    private String tripName;
    private String description;
    private Long themeId;
    private String representativeImageUrl;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
