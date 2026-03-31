package com.app.cinema.dto.screening;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@Builder
public class CreateScreeningDTO {

    @NotNull(message = "Musisz podać ID filmu")
    @Positive
    private Long movieId;

    @NotNull(message = "Musisz podać ID sali")
    @Positive
    private Long hallId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    @NotNull
    @DecimalMin(value = "0.01", message = "Cena nie może być mniejsza niż 1 grosz")
    private BigDecimal price;


}
