package com.app.cinema.service;

import com.app.cinema.dto.screening.CreateScreeningDTO;
import com.app.cinema.dto.screening.EditScreeningDTO;
import com.app.cinema.dto.screening.ScreeningDTO;
import com.app.cinema.model.Hall;
import com.app.cinema.model.Movie;
import com.app.cinema.model.Screening;
import com.app.cinema.repository.HallRepository;
import com.app.cinema.repository.MovieRepository;
import com.app.cinema.repository.ScreeningRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;

    public List<ScreeningDTO> getAllScreenings() {
        return screeningRepository.findAll().stream().map(this::toDTO).toList();
    }

    private ScreeningDTO toDTO(Screening screening) {
        return ScreeningDTO.builder()
                .id(screening.getId())
                .movieId(screening.getMovie().getId())
                .hallId(screening.getHall().getId())
                .startTime(screening.getStartTime())
                .endTime(screening.getEndTime())
                .price(screening.getPrice())
                .build();
    }
    
    public ScreeningDTO createScreening(CreateScreeningDTO createScreeningDTO) {
        Movie movie = movieRepository.findById(createScreeningDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("Nie istnieje taki film"));

        Hall hall = hallRepository.findById(createScreeningDTO.getHallId())
                .orElseThrow(() -> new RuntimeException("Nie istnieje taka sala"));


        if (createScreeningDTO.getStartTime().equals(createScreeningDTO.getEndTime())) {
            throw new RuntimeException("Nieprawidłowy czas: rozpoczęcie i zakończenie nie mogą być takie same");
        } else if (createScreeningDTO.getStartTime().isAfter(createScreeningDTO.getEndTime())) {
            throw new RuntimeException("seans nie może kończyć się przed godziną rozpoczęcia");
        }

        Screening screening = Screening.builder()
                .movie(movie)
                .hall(hall)
                .startTime(createScreeningDTO.getStartTime())
                .endTime(createScreeningDTO.getEndTime())
                .price(createScreeningDTO.getPrice())
                .build();

        Screening savedScreening = screeningRepository.save(screening);
        return toDTO(savedScreening);

    }

    public ScreeningDTO editScreening(Long id, EditScreeningDTO editScreeningDTO){

        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono seansu o podanym ID"));
        
        if (editScreeningDTO.getStartTime().equals(editScreeningDTO.getEndTime())) {
            throw new RuntimeException("Nieprawidłowy czas: rozpoczęcie i zakończenie nie mogą być takie same");
        } else if (editScreeningDTO.getStartTime().isAfter(editScreeningDTO.getEndTime())) {
            throw new RuntimeException("seans nie może kończyć się przed godziną rozpoczęcia");
        }

        screening.setEndTime(editScreeningDTO.getEndTime());
        screening.setStartTime(editScreeningDTO.getStartTime());
        screening.setPrice(editScreeningDTO.getPrice());

        Screening updatedScreening=screeningRepository.save(screening);

        return toDTO(updatedScreening);


    }
}
