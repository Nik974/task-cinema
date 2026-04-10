package com.app.cinema.dto.movie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditMovieDTO {
    @NotBlank
    @Size(min = 1, max = 100)
    private String title;

    @Size(max = 200)
    private String description;

    @NotNull
    @Positive
    private int durationMinutes;

    @NotNull
    private LocalDate releaseDate;
}
