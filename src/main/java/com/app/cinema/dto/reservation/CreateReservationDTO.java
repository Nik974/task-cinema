package com.app.cinema.dto.reservation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateReservationDTO{

    @NotNull
    private Long screeningId;

    @NotEmpty
    private List<Long> seatIds;
}