package com.app.cinema.dto.seat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SeatDTO {

    private Long id;

    @NotNull
    private Long hallId;

    @Min(1)
    private int rowNumber;

    @Min(1)
    private int seatNumber;

}
