package com.app.cinema.controller;


import com.app.cinema.dto.hall.CreateHallDTO;
import com.app.cinema.dto.hall.HallDTO;
import com.app.cinema.dto.seat.SeatDTO;
import com.app.cinema.service.HallService;
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
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;
    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<List<HallDTO>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDTO> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    // GET /api/halls/1/seats - wszystkie miejsca w sali
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDTO>> getSeatsByHall(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByHallId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HallDTO> addHall(@Valid @RequestBody CreateHallDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hallService.addNewHall(dto));
    }
}