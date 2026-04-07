package com.app.cinema.controller;


import com.app.cinema.dto.screening.CreateScreeningDTO;
import com.app.cinema.dto.screening.ScreeningDTO;
import com.app.cinema.dto.seat.SeatDTO;
import com.app.cinema.service.ScreeningService;
import com.app.cinema.service.SeatService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final SeatService seatService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<ScreeningDTO>> getAllScreenings() {
        return ResponseEntity.ok(screeningService.getAllScreenings());
    }

    // GET /api/screenings/1/taken-seats - zajęte miejsca na seans
    @GetMapping("/{id}/taken-seats")
    public ResponseEntity<List<SeatDTO>> getTakenSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getTakenSeatsForScreening(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningDTO> addScreening(@Valid @RequestBody CreateScreeningDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(screeningService.createScreening(dto));
    }
}
