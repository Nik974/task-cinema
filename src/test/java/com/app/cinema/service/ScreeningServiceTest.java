package com.app.cinema.service;

import com.app.cinema.dto.screening.CreateScreeningDTO;
import com.app.cinema.dto.screening.ScreeningDTO;
import com.app.cinema.model.Hall;
import com.app.cinema.model.Movie;
import com.app.cinema.model.Screening;
import com.app.cinema.repository.HallRepository;
import com.app.cinema.repository.MovieRepository;
import com.app.cinema.repository.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private ScreeningService screeningService;

    private Movie movie;
    private Hall hall;
    private Screening screening;

    private static final LocalDateTime START = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime END = LocalDateTime.now().plusDays(1).plusHours(2);

    @BeforeEach
    void setUp() {
        movie = Movie.builder().id(1L).title("test").build();

        hall = Hall.builder().id(1L).name("Sala A").totalRows(10).seatsPerRow(15).build();

        screening = Screening.builder()
                .id(1L)
                .movie(movie)
                .hall(hall)
                .startTime(START)
                .endTime(END)
                .price(new BigDecimal("25.00"))
                .build();
    }

    @Test
    void getAllScreenings_ShouldReturnListOfDTOs() {
        when(screeningRepository.findAll()).thenReturn(List.of(screening));

        List<ScreeningDTO> result = screeningService.getAllScreenings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getMovieId()).isEqualTo(1L);
        assertThat(result.get(0).getHallId()).isEqualTo(1L);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("25.00");
        verify(screeningRepository).findAll();
    }

    @Test
    void getAllScreenings_ShouldReturnEmptyList_WhenNoScreeningsExist() {
        when(screeningRepository.findAll()).thenReturn(List.of());

        assertThat(screeningService.getAllScreenings()).isEmpty();
    }

    @Test
    void createScreening_ShouldSaveAndReturnDTO_WhenDataIsValid() {
        CreateScreeningDTO dto = CreateScreeningDTO.builder()
                .movieId(1L).hallId(1L)
                .startTime(START).endTime(END)
                .price(new BigDecimal("25.00"))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(screeningRepository.save(any(Screening.class))).thenReturn(screening);

        ScreeningDTO result = screeningService.createScreening(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMovieId()).isEqualTo(1L);
        assertThat(result.getHallId()).isEqualTo(1L);
        assertThat(result.getStartTime()).isEqualTo(START);
        assertThat(result.getEndTime()).isEqualTo(END);
        assertThat(result.getPrice()).isEqualByComparingTo("25.00");

        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    void createScreening_ShouldThrow_WhenMovieNotFound() {
        CreateScreeningDTO dto = CreateScreeningDTO.builder()
                .movieId(99L).hallId(1L)
                .startTime(START).endTime(END)
                .price(new BigDecimal("25.00"))
                .build();

        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("film");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    void createScreening_ShouldThrow_WhenHallNotFound() {
        CreateScreeningDTO dto = CreateScreeningDTO.builder()
                .movieId(1L).hallId(99L)
                .startTime(START).endTime(END)
                .price(new BigDecimal("25.00"))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sala");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    void createScreening_ShouldThrow_WhenStartTimeEqualsEndTime() {
        CreateScreeningDTO dto = CreateScreeningDTO.builder()
                .movieId(1L).hallId(1L)
                .startTime(START).endTime(START)   // takie same
                .price(new BigDecimal("25.00"))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("takie same");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    void createScreening_ShouldThrow_WhenStartTimeIsAfterEndTime() {
        CreateScreeningDTO dto = CreateScreeningDTO.builder()
                .movieId(1L).hallId(1L)
                .startTime(END).endTime(START)
                .price(new BigDecimal("25.00"))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("kończyć się przed");

        verify(screeningRepository, never()).save(any());
    }


}