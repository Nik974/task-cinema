package com.app.cinema.service;

import com.app.cinema.dto.movie.CreateMovieDTO;
import com.app.cinema.dto.movie.EditMovieDTO;
import com.app.cinema.dto.movie.MovieDTO;
import com.app.cinema.model.Movie;
import com.app.cinema.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDTO> getAllMovies() {

        return movieRepository.findAll().stream().map(this::toDTO).toList();
    }

    private MovieDTO toDTO(Movie movie) {
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .build();
    }

    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Film o id " + id + " nie istnieje"));
        return toDTO(movie);
    }

    @Transactional
    public MovieDTO addNewMovie(CreateMovieDTO movieDTO) {

        if (movieRepository.existsByTitle(movieDTO.getTitle())) {
            throw new IllegalArgumentException("Film o takim tytule już istnieje!");
        }
        Movie movie = Movie.builder()
                .title(movieDTO.getTitle())
                .description(movieDTO.getDescription())
                .durationMinutes(movieDTO.getDurationMinutes())
                .releaseDate(movieDTO.getReleaseDate())
                .build();

        Movie savedMovie = movieRepository.save(movie);

        return toDTO(savedMovie);
    }

    @Transactional
    public MovieDTO editMovie(Long id, EditMovieDTO editMovieDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie ma takiego filmu"));

        movie.setDescription(editMovieDTO.getDescription());
        movie.setTitle(editMovieDTO.getTitle());
        movie.setDurationMinutes(editMovieDTO.getDurationMinutes());
        movie.setReleaseDate(editMovieDTO.getReleaseDate());
        Movie updatedMovie = movieRepository.save(movie);
        return toDTO(updatedMovie);
    }


}
