package com.app.cinema.controller;

import com.app.cinema.dto.movie.CreateMovieDTO;
import com.app.cinema.dto.movie.EditMovieDTO;
import com.app.cinema.dto.movie.MovieDTO;
import com.app.cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    // GET /api/movies - wszyscy mogą
    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    // GET /api/movies/1
    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    // POST /api/movies - tylko ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> addMovie(@Valid @RequestBody CreateMovieDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.addNewMovie(dto));
    }

    // PUT /api/movies/1 - tylko ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> editMovie(@PathVariable Long id,
                                              @Valid @RequestBody EditMovieDTO dto) {
        return ResponseEntity.ok(movieService.editMovie(id, dto));
    }
}
