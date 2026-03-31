package com.app.cinema.dto.hall;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HallDTO {

    @NotNull
    private long id;

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Min(1)
    @Max(100)
    private int rows;

    @Min(2)
    @Max(100)
    private int seatsPerRow;
}
