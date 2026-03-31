package com.app.cinema.service;

import com.app.cinema.dto.movie.CreateMovieDTO;
import com.app.cinema.dto.movie.EditMovieDTO;
import com.app.cinema.dto.movie.MovieDTO;
import com.app.cinema.model.Movie;
import com.app.cinema.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;
    private Movie movie1;

    @BeforeEach
    void setUp() {
        movie1 = Movie.builder()
                .id(1L)
                .title("test-1")
                .description("TEST-description")
                .durationMinutes(25)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();

    }

    @Test
    void getAllMovies_ShouldReturnListOfMovies() {
        Movie movie2 = Movie.builder()
                .id(1L)
                .title("test-2")
                .description("TEST-description")
                .durationMinutes(55)
                .releaseDate(LocalDate.of(1999, 12, 1))
                .build();

        when(movieRepository.findAll()).thenReturn(List.of(movie1, movie2));
        List<MovieDTO> result = movieService.getAllMovies();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MovieDTO::getTitle).containsExactly("test-1", "test-2");

        verify(movieRepository).findAll();
    }

    @Test
    void getAllMovies_ShouldReturnEmptyList() {

        when(movieRepository.findAll()).thenReturn(List.of());
        List<MovieDTO> result = movieService.getAllMovies();

        assertThat(result).isEmpty();

        verify(movieRepository).findAll();
    }

    @Test
    void getMovieById_ShouldReturnMovieDTO_WhenMovieExists() {

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie1));

        MovieDTO result = movieService.getMovieById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("test-1");
        assertThat(result.getDescription()).isEqualTo("TEST-description");
        assertThat(result.getDurationMinutes()).isEqualTo(25);

        verify(movieRepository).findById(1L);
    }

    @Test
    void getMovieById_ShouldThrowException() {
        when(movieRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById(9L))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("9");

        verify(movieRepository).findById(9L);
    }

    @Test
    void addNewMovie_ShouldSave() {

        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .title("Test-Name-1")
                .description("Test-Descr")
                .durationMinutes(56)
                .releaseDate(LocalDate.of(2002, 2,3))
                .build();

        Movie savedMovie =Movie.builder()
                .id(1L)
                .title("Test-Name-1")
                .description("Test-Desc")
                .durationMinutes(56)
                .releaseDate(LocalDate.of(2002,2,3))
                .build();


        when(movieRepository.existsByTitle("Test-Name-1")).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        MovieDTO result = movieService.addNewMovie(createMovieDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.of(2002,2,3));
        assertThat(result.getTitle()).isEqualTo("Test-Name-1");
        assertThat(result.getDescription()).isEqualTo("Test-Desc");
        assertThat(result.getDurationMinutes()).isEqualTo(56);

        verify(movieRepository).existsByTitle("Test-Name-1");
        verify(movieRepository).save(any(Movie.class));

    }

    @Test
    void addNewMovie_ShouldThrowException(){
        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .title("Test-Name-1")
                .description("Test-Descr")
                .durationMinutes(56)
                .releaseDate(LocalDate.of(2003, 3,3))
                .build();


        when(movieRepository.existsByTitle("Test-Name-1")).thenReturn(true);

        assertThatThrownBy(()->movieService.addNewMovie(createMovieDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Film o takim tytule już istnieje!");

        verify(movieRepository).existsByTitle("Test-Name-1");
        verify(movieRepository, never()).save(any());
    }

    @Test
    void editMovie_ShouldSaveEditedMovie() {
        EditMovieDTO editMovieDTO = EditMovieDTO.builder()
                .title("Test-Name-1-edited")
                .description("edited-description")
                .durationMinutes(50)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();

        Movie updatedMovie = Movie.builder()
                .id(1L)
                .title("Test-Name-1-edited")
                .description("edited-description")
                .durationMinutes(50)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie1));
        when(movieRepository.save(movie1)).thenReturn(updatedMovie);

        MovieDTO result = movieService.editMovie(1L,editMovieDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("edited-description");
        assertThat(result.getTitle()).isEqualTo("Test-Name-1-edited");
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.of(2000,1,1));
        assertThat(result.getDurationMinutes()).isEqualTo(50);

        verify(movieRepository).findById(1L);
        verify(movieRepository).save(movie1);

    }

    @Test
    void editMovie_ShouldThrowException_WhenMovieNotFound() {
        EditMovieDTO editMovieDTO = EditMovieDTO.builder()
                .title("edited")
                .description("edited")
                .durationMinutes(50)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();

        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.editMovie(99L, editMovieDTO))
                .isInstanceOf(EntityNotFoundException.class);

        verify(movieRepository, never()).save(any());
    }
}