package com.app.cinema.service;


import com.app.cinema.dto.seat.SeatDTO;
import com.app.cinema.model.Seat;
import com.app.cinema.repository.ReservationSeatRepository;
import com.app.cinema.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    public List<SeatDTO> getSeatsByHallId(Long hallId) {
        return seatRepository.findByHallId(hallId)
                .stream()
                .map(this::toDTO)
                .toList();

    }
    public List<SeatDTO> getTakenSeatsForScreening(Long screeningId) {
        return reservationSeatRepository
                .findOccupiedSeatsForScreening(screeningId)
                .stream()
                .map(rs -> toDTO(rs.getSeat()))
                .toList();
    }


    private SeatDTO toDTO(Seat seat) {
        return new SeatDTO(
                seat.getId(),
                seat.getHall().getId(),
                seat.getRowNumber(),
                seat.getSeatNumber()
        );
    }



}
