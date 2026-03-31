package com.app.cinema.dto.reservation;

import com.app.cinema.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private Long id;
    private String username;
    private Long screeningId;
    private String movieTitle;
    private String hallName;
    private LocalDateTime screeningTime;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private List<SeatInfo> seats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeatInfo {
        private int rowNumber;
        private int seatNumber;
    }
}