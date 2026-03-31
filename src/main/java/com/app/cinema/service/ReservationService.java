package com.app.cinema.service;

import com.app.cinema.dto.reservation.CreateReservationDTO;
import com.app.cinema.dto.reservation.ReservationDTO;
import com.app.cinema.model.*;
import com.app.cinema.repository.ReservationRepository;
import com.app.cinema.repository.ReservationSeatRepository;
import com.app.cinema.repository.ScreeningRepository;
import com.app.cinema.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;

    // Wszystkie rezerwacje - dla admina
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream().map(this::toDTO).toList();
    }

    // Rezerwacje po screeningId - dla admina
    public List<ReservationDTO> getByScreening(Long screeningId) {
        return reservationRepository.findByScreeningId(screeningId)
                .stream().map(this::toDTO).toList();
    }

    // Rezerwacje po username - dla admina
    public List<ReservationDTO> getByUsername(String username) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getUser().getUsername()
                        .toLowerCase()
                        .contains(username.toLowerCase()))
                .map(this::toDTO)
                .toList();
    }

    // Potwierdzenie rezerwacji - tylko admin
    @Transactional
    public ReservationDTO confirm(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rezerwacja nie istnieje"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Nie można potwierdzić anulowanej rezerwacji");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        return toDTO(reservationRepository.save(reservation));
    }

    public List<ReservationDTO> getMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId)
                .stream().map(this::toDTO).toList();
    }

    // Anulowanie rezerwacji - admin lub właściciel
    @Transactional
    public ReservationDTO cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rezerwacja nie istnieje"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Rezerwacja jest już anulowana");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return toDTO(reservationRepository.save(reservation));
    }

    private ReservationDTO toDTO(Reservation r) {
        List<ReservationDTO.SeatInfo> seats = r.getReservationSeats().stream()
                .map(rs -> new ReservationDTO.SeatInfo(
                        rs.getSeat().getRowNumber(),
                        rs.getSeat().getSeatNumber()
                ))
                .toList();

        return ReservationDTO.builder()
                .id(r.getId())
                .username(r.getUser().getUsername())
                .screeningId(r.getScreening().getId())
                .movieTitle(r.getScreening().getMovie().getTitle())
                .hallName(r.getScreening().getHall().getName())
                .screeningTime(r.getScreening().getStartTime())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .seats(seats)
                .build();
    }
    @Transactional
    public ReservationDTO createReservation(CreateReservationDTO request, User user) {
        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("Seans nie istnieje"));

        // Sprawdź czy miejsca nie są już zajęte
        List<Long> takenIds = reservationSeatRepository
                .findOccupiedSeatsForScreening(request.getScreeningId())
                .stream()
                .map(rs -> rs.getSeat().getId())
                .toList();

        List<Long> conflict = request.getSeatIds().stream()
                .filter(takenIds::contains)
                .toList();

        if (!conflict.isEmpty()) {
            throw new IllegalStateException("Niektóre miejsca są już zajęte");
        }

        // Utwórz rezerwację
        Reservation reservation = Reservation.builder()
                .user(user)
                .screening(screening)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        // Dodaj miejsca do rezerwacji
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        List<ReservationSeat> reservationSeats = seats.stream()
                .map(seat -> ReservationSeat.builder()
                        .reservation(saved)
                        .seat(seat)
                        .screening(screening)
                        .build())
                .toList();

        reservationSeatRepository.saveAll(reservationSeats);

        return toDTO(saved);
    }
    @Transactional
    public ReservationDTO cancelByUser(Long id, User user) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rezerwacja nie istnieje"));

        // User może anulować tylko własną rezerwację, admin może każdą
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = reservation.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Brak uprawnień do anulowania tej rezerwacji");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Rezerwacja jest już anulowana");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return toDTO(reservationRepository.save(reservation));
    }
}
