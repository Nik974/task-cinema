package com.app.cinema.service;


import com.app.cinema.dto.hall.CreateHallDTO;
import com.app.cinema.dto.hall.HallDTO;
import com.app.cinema.model.Hall;
import com.app.cinema.model.Seat;
import com.app.cinema.repository.HallRepository;
import com.app.cinema.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HallService {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    public HallService(HallRepository hallRepository, SeatRepository seatRepository) {
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
    }

    private HallDTO toDTO(Hall hall) {
        return HallDTO.builder()
                .id(hall.getId())
                .name(hall.getName())
                .rows(hall.getTotalRows())
                .seatsPerRow(hall.getSeatsPerRow())
                .build();

    }

    public HallDTO getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hall o id" + id + "nie istnieje"));
        return toDTO(hall);
    }

    public HallDTO getHallByName(String name) {
        Hall hall = hallRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("Nie ma takiej sali"));
        return toDTO(hall);
    }

    public List<HallDTO> getAllHalls() {
        return hallRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public HallDTO addNewHall(CreateHallDTO hallDTO) {
        if (hallRepository.existsByName(hallDTO.getName())) {
            throw new IllegalArgumentException("Sala z taką nazwą już istnieje");
        }

        Hall hall = Hall.builder()
                .name(hallDTO.getName())
                .totalRows(hallDTO.getRows())
                .seatsPerRow(hallDTO.getSeatsPerRow())
                .build();

        Hall savedHall = hallRepository.save(hall);

        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= hallDTO.getRows(); row++) {
            for (int seat = 1; seat <= hallDTO.getSeatsPerRow(); seat++) {
                seats.add(Seat.builder()
                        .hall(savedHall)
                        .rowNumber(row)
                        .seatNumber(seat)
                        .build());
            }
        }
        seatRepository.saveAll(seats);

        return toDTO(savedHall);
    }

}
