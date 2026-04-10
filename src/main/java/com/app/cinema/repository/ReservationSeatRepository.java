package com.app.cinema.repository;

import com.app.cinema.model.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    // zajęte miejsca na konkretny seans
    @Query("""
            SELECT rs FROM ReservationSeat rs
            WHERE rs.screening.id = :screeningId
            AND rs.reservation.status <> 'CANCELLED'
            """)
    List<ReservationSeat> findOccupiedSeatsForScreening(
            @Param("screeningId") Long screeningId
    );

    // sprawdzenie czy konkretne miejsce jest już zajęte na dany seans
    @Query("""
            SELECT COUNT(rs) > 0 FROM ReservationSeat rs
            WHERE rs.seat.id = :seatId
            AND rs.screening.id = :screeningId
            AND rs.reservation.status <> 'CANCELLED'
            """)
    boolean isSeatOccupied(
            @Param("seatId") Long seatId,
            @Param("screeningId") Long screeningId
    );
}
