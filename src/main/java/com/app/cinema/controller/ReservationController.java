package com.app.cinema.controller;

import com.app.cinema.dto.reservation.CreateReservationDTO;
import com.app.cinema.dto.reservation.ReservationDTO;
import com.app.cinema.model.Role;
import com.app.cinema.model.User;
import com.app.cinema.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservations(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long screeningId
    ) {
        if (username != null && !username.isBlank()) {
            return ResponseEntity.ok(reservationService.getByUsername(username));
        }
        if (screeningId != null) {
            return ResponseEntity.ok(reservationService.getByScreening(screeningId));
        }
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    // PUT /api/reservations/1/confirm - potwierdzenie (admin)
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationDTO> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationDTO> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reservationService.cancelByUser(id, user));
    }

    // Rezerwacje zalogowanego usera
    @GetMapping("/my")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reservationService.getMyReservations(user.getId()));
    }

    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody CreateReservationDTO request,
            @AuthenticationPrincipal User user) {
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request, user));
    }
}
