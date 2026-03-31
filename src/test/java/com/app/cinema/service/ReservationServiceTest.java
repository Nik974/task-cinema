package com.app.cinema.service;

import com.app.cinema.dto.reservation.CreateReservationDTO;
import com.app.cinema.dto.reservation.ReservationDTO;
import com.app.cinema.model.*;
import com.app.cinema.repository.ReservationRepository;
import com.app.cinema.repository.ReservationSeatRepository;
import com.app.cinema.repository.ScreeningRepository;
import com.app.cinema.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationSeatRepository reservationSeatRepository;
    @Mock private ScreeningRepository screeningRepository;
    @Mock private SeatRepository seatRepository;

    @InjectMocks
    private ReservationService reservationService;
    private User user;
    private User adminUser;
    private Hall hall;
    private Movie movie;
    private Screening screening;
    private Seat seat1;
    private Seat seat2;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("jan")
                .email("jan@wp.pl")
                .passwordHash("hash")
                .role(Role.USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@cinema.pl")
                .passwordHash("hash")
                .role(Role.ADMIN)
                .build();

        hall = Hall.builder()
                .id(1L)
                .name("Sala 1")
                .totalRows(10)
                .seatsPerRow(15)
                .build();

        movie = Movie.builder()
                .id(1L)
                .title("Inception")
                .durationMinutes(148)
                .build();

        screening = Screening.builder()
                .id(1L)
                .movie(movie)
                .hall(hall)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(BigDecimal.valueOf(25.00))
                .build();

        seat1 = Seat.builder()
                .id(10L)
                .hall(hall)
                .rowNumber(1)
                .seatNumber(1)
                .build();

        seat2 = Seat.builder()
                .id(11L)
                .hall(hall)
                .rowNumber(1)
                .seatNumber(2)
                .build();

        reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .screening(screening)
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .reservationSeats(List.of())
                .build();
    }


    @Test
    void createReservation_poprawneData_tworzyRezerwacje() {

        CreateReservationDTO request = new CreateReservationDTO();
        request.setScreeningId(1L);
        request.setSeatIds(List.of(10L, 11L));

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(screening));
        when(reservationSeatRepository.findOccupiedSeatsForScreening(1L))
                .thenReturn(List.of()); // brak zajętych miejsc
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(seatRepository.findAllById(List.of(10L, 11L)))
                .thenReturn(List.of(seat1, seat2));
        when(reservationSeatRepository.saveAll(any())).thenReturn(List.of());


        ReservationDTO result = reservationService.createReservation(request, user);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationSeatRepository).saveAll(any());
    }

    @Test
    void createReservation_seansNieIstnieje_rzucaEntityNotFoundException() {

        CreateReservationDTO request = new CreateReservationDTO();
        request.setScreeningId(99L);
        request.setSeatIds(List.of(10L));

        when(screeningRepository.findById(99L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> reservationService.createReservation(request, user))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Seans nie istnieje");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_miejsceJuzZajete_rzucaIllegalStateException() {

        CreateReservationDTO request = new CreateReservationDTO();
        request.setScreeningId(1L);
        request.setSeatIds(List.of(10L)); // seat1 będzie zajęty

        ReservationSeat takenSeat = ReservationSeat.builder()
                .seat(seat1)
                .screening(screening)
                .reservation(reservation)
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(screening));
        when(reservationSeatRepository.findOccupiedSeatsForScreening(1L))
                .thenReturn(List.of(takenSeat));


        assertThatThrownBy(() -> reservationService.createReservation(request, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("zajęte");

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void getMyReservations_zwracaRezerwacjeUsera() {

        when(reservationRepository.findByUserId(1L))
                .thenReturn(List.of(reservation));


        List<ReservationDTO> result = reservationService.getMyReservations(1L);


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("jan");
    }

    @Test
    void getMyReservations_brakRezerwacji_zwracaPustaListe() {

        when(reservationRepository.findByUserId(99L)).thenReturn(List.of());


        List<ReservationDTO> result = reservationService.getMyReservations(99L);

        assertThat(result).isEmpty();
    }


    @Test
    void getAllReservations_zwracaWszystkieRezerwacje() {
        Reservation reservation2 = Reservation.builder()
                .id(2L).user(adminUser).screening(screening)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .reservationSeats(List.of())
                .build();

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation, reservation2));


        List<ReservationDTO> result = reservationService.getAllReservations();

        assertThat(result).hasSize(2);
    }

    @Test
    void getByScreening_zwracaRezerwacjeDlaSeansu() {

        when(reservationRepository.findByScreeningId(1L))
                .thenReturn(List.of(reservation));


        List<ReservationDTO> result = reservationService.getByScreening(1L);


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScreeningId()).isEqualTo(1L);
    }

    @Test
    void getByUsername_zwracaRezerwacjePoNazwieUsera() {

        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        List<ReservationDTO> result = reservationService.getByUsername("jan");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("jan");
    }

    @Test
    void getByUsername_brakDopasowania_zwracaPustaListe() {

        when(reservationRepository.findAll()).thenReturn(List.of(reservation));


        List<ReservationDTO> result = reservationService.getByUsername("nieistniejacy");

        assertThat(result).isEmpty();
    }


    @Test
    void confirm_rezerwacjaPending_zmieniaStatusNaConfirmed() {

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);


        ReservationDTO result = reservationService.confirm(1L);

        verify(reservationRepository).save(argThat(r ->
                r.getStatus() == ReservationStatus.CONFIRMED
        ));
    }

    @Test
    void confirm_rezerwacjaAnulowana_rzucaIllegalStateException() {

        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirm(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("anulowanej");
    }

    @Test
    void confirm_nieistniejacaRezerwacja_rzucaEntityNotFoundException() {

        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.confirm(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }


    @Test
    void cancel_zmieniaStatusNaCancelled() {

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);


        reservationService.cancel(1L);

        verify(reservationRepository).save(argThat(r ->
                r.getStatus() == ReservationStatus.CANCELLED
        ));
    }

    @Test
    void cancel_juzAnulowana_rzucaIllegalStateException() {

        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("już anulowana");
    }

    @Test
    void cancelByUser_wlasciciel_mozeCancelowac() {

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);


        reservationService.cancelByUser(1L, user);

        verify(reservationRepository).save(argThat(r ->
                r.getStatus() == ReservationStatus.CANCELLED
        ));
    }

    @Test
    void cancelByUser_admin_mozeCancelowacCudzaRezerwacje() {

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);


        reservationService.cancelByUser(1L, adminUser);

        verify(reservationRepository).save(any());
    }

    @Test
    void cancelByUser_innyUser_rzucaAccessDeniedException() {
        User otherUser = User.builder()
                .id(99L).username("obcy").role(Role.USER).build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelByUser(1L, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("uprawnień");
    }
}
