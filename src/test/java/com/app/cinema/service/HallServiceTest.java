package com.app.cinema.service;

import com.app.cinema.dto.hall.CreateHallDTO;
import com.app.cinema.model.Hall;
import com.app.cinema.repository.HallRepository;
import com.app.cinema.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.cinema.dto.hall.HallDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;
    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private HallService hallService;

    private Hall hall;

    @BeforeEach
    void setUp() {
        hall = Hall.builder()
                .id(1L)
                .name("Sala A")
                .totalRows(10)
                .seatsPerRow(15)
                .build();
    }


    @Test
    void getHallById_ShouldReturnHallDTO_WhenHallExists() {
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));

        HallDTO result = hallService.getHallById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sala A");
        assertThat(result.getRows()).isEqualTo(10);
        assertThat(result.getSeatsPerRow()).isEqualTo(15);
        verify(hallRepository).findById(1L);
    }

    @Test
    void getHallById_ShouldThrowEntityNotFoundException_WhenHallNotFound() {
        when(hallRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.getHallById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(hallRepository).findById(99L);
    }


    @Test
    void getHallByName_ShouldReturnHallDTO_WhenHallExists() {
        when(hallRepository.findByNameIgnoreCase("sala a")).thenReturn(Optional.of(hall));

        HallDTO result = hallService.getHallByName("sala a");

        assertThat(result.getName()).isEqualTo("Sala A");
        verify(hallRepository).findByNameIgnoreCase("sala a");
    }

    @Test
    void getHallByName_ShouldThrowEntityNotFoundException_WhenHallNotFound() {
        when(hallRepository.findByNameIgnoreCase("Nieznana")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.getHallByName("Nieznana"))
                .isInstanceOf(EntityNotFoundException.class);

        verify(hallRepository).findByNameIgnoreCase("Nieznana");
    }


    @Test
    void getAllHalls_ShouldReturnListOfHallDTOs() {
        Hall hall2 = Hall.builder()
                .id(2L)
                .name("Sala B")
                .totalRows(8)
                .seatsPerRow(12)
                .build();

        when(hallRepository.findAll()).thenReturn(List.of(hall, hall2));

        List<HallDTO> result = hallService.getAllHalls();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(HallDTO::getName)
                .containsExactly("Sala A", "Sala B");
        verify(hallRepository).findAll();
    }

    @Test
    void getAllHalls_ShouldReturnEmptyList_WhenNoHallsExist() {
        when(hallRepository.findAll()).thenReturn(List.of());

        List<HallDTO> result = hallService.getAllHalls();

        assertThat(result).isEmpty();
        verify(hallRepository).findAll();
    }


    @Test
    void addNewHall_ShouldSaveAndReturnHallDTO_WhenNameIsUnique() {
        CreateHallDTO createDTO = CreateHallDTO.builder()
                .name("Sala C")
                .rows(5)
                .seatsPerRow(20)
                .build();

        Hall savedHall = Hall.builder()
                .id(3L)
                .name("Sala C")
                .totalRows(5)
                .seatsPerRow(20)
                .build();

        when(hallRepository.existsByName("Sala C")).thenReturn(false);
        when(hallRepository.save(any(Hall.class))).thenReturn(savedHall);
        when(seatRepository.saveAll(any())).thenReturn(List.of());

        HallDTO result = hallService.addNewHall(createDTO);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Sala C");
        assertThat(result.getRows()).isEqualTo(5);
        assertThat(result.getSeatsPerRow()).isEqualTo(20);

        verify(hallRepository).existsByName("Sala C");
        verify(hallRepository).save(any(Hall.class));
    }

    @Test
    void addNewHall_ShouldThrowRuntimeException_WhenNameAlreadyExists() {
        CreateHallDTO createDTO = CreateHallDTO.builder()
                .name("Sala A")
                .rows(5)
                .seatsPerRow(10)
                .build();

        when(hallRepository.existsByName("Sala A")).thenReturn(true);

        assertThatThrownBy(() -> hallService.addNewHall(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sala z taką nazwą już istnieje");

        verify(hallRepository).existsByName("Sala A");
        verify(hallRepository, never()).save(any());
    }
}


