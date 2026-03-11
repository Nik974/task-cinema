package com.app.cinema.repository;

import com.app.cinema.model.Reservation;
import com.app.cinema.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    List<Reservation> findByScreeningId(Long screeningId);

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    // liczba zajętych miejsc na seans
    @Query("""
            SELECT COUNT(rs) FROM ReservationSeat rs
            WHERE rs.screening.id = :screeningId
            AND rs.reservation.status <> 'CANCELLED'
            """)
    int countOccupiedSeats(@Param("screeningId") Long screeningId);
}
