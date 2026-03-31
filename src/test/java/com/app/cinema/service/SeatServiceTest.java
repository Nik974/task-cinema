package com.app.cinema.service;

import com.app.cinema.dto.seat.SeatDTO;
import com.app.cinema.model.*;
import com.app.cinema.repository.ReservationSeatRepository;
import com.app.cinema.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock private SeatRepository seatRepository;
    @Mock private ReservationSeatRepository reservationSeatRepository;

    @InjectMocks
    private SeatService seatService;

    private Hall hall;
    private Seat seat1;
    private Seat seat2;

    @BeforeEach
    void setUp() {
        hall = Hall.builder()
                .id(1L)
                .name("Sala 1")
                .totalRows(5)
                .seatsPerRow(10)
                .build();

        seat1 = Seat.builder()
                .id(10L)
                .hall(hall)
                .rowNumber(1)
                .seatNumber(1)
                .build();

        seat2 = Seat.builder()
                .id(11L)
                .hall(hall)
                .rowNumber(1)
                .seatNumber(2)
                .build();
    }


    @Test
    void getSeatsByHallId_zwracaMiejscaDlaSali() {
        when(seatRepository.findByHallId(1L)).thenReturn(List.of(seat1, seat2));

        List<SeatDTO> result = seatService.getSeatsByHallId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getHallId()).isEqualTo(1L);
        assertThat(result.get(0).getRowNumber()).isEqualTo(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(11L);
        verify(seatRepository).findByHallId(1L);
    }

    @Test
    void getSeatsByHallId_pustaSala_zwracaPustaListe() {

        when(seatRepository.findByHallId(99L)).thenReturn(List.of());


        List<SeatDTO> result = seatService.getSeatsByHallId(99L);


        assertThat(result).isEmpty();
        verify(seatRepository).findByHallId(99L);
    }

    @Test
    void getSeatsByHallId_poprawnieMappujePolaDTO() {

        when(seatRepository.findByHallId(1L)).thenReturn(List.of(seat1));


        SeatDTO dto = seatService.getSeatsByHallId(1L).get(0);

        assertThat(dto.getId()).isEqualTo(seat1.getId());
        assertThat(dto.getHallId()).isEqualTo(hall.getId());
        assertThat(dto.getRowNumber()).isEqualTo(seat1.getRowNumber());
        assertThat(dto.getSeatNumber()).isEqualTo(seat1.getSeatNumber());
    }


    @Test
    void getTakenSeatsForScreening_zwracaZajeteMiejsca() {
        Screening screening = Screening.builder().id(1L).build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ReservationSeat rs1 = ReservationSeat.builder()
                .id(1L)
                .seat(seat1)
                .screening(screening)
                .reservation(reservation)
                .build();

        ReservationSeat rs2 = ReservationSeat.builder()
                .id(2L)
                .seat(seat2)
                .screening(screening)
                .reservation(reservation)
                .build();

        when(reservationSeatRepository.findOccupiedSeatsForScreening(1L))
                .thenReturn(List.of(rs1, rs2));

        List<SeatDTO> result = seatService.getTakenSeatsForScreening(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(1).getId()).isEqualTo(11L);
        verify(reservationSeatRepository).findOccupiedSeatsForScreening(1L);
    }

    @Test
    void getTakenSeatsForScreening_brakZajetychMiejsc_zwracaPustaListe() {
        when(reservationSeatRepository.findOccupiedSeatsForScreening(1L))
                .thenReturn(List.of());

        List<SeatDTO> result = seatService.getTakenSeatsForScreening(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTakenSeatsForScreening_poprawnieMappujeMiejscaZReservationSeat() {
        ReservationSeat rs = ReservationSeat.builder()
                .id(1L)
                .seat(seat1)
                .screening(Screening.builder().id(5L).build())
                .reservation(Reservation.builder().id(1L).build())
                .build();

        when(reservationSeatRepository.findOccupiedSeatsForScreening(5L))
                .thenReturn(List.of(rs));

        SeatDTO dto = seatService.getTakenSeatsForScreening(5L).get(0);

        assertThat(dto.getId()).isEqualTo(seat1.getId());
        assertThat(dto.getHallId()).isEqualTo(hall.getId());
        assertThat(dto.getRowNumber()).isEqualTo(seat1.getRowNumber());
        assertThat(dto.getSeatNumber()).isEqualTo(seat1.getSeatNumber());
    }
}