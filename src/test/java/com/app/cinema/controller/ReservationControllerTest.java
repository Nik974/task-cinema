package com.app.cinema.controller;
import com.app.cinema.dto.auth.AuthResponse;
import com.app.cinema.dto.auth.LoginRequest;
import com.app.cinema.dto.auth.RegisterRequest;
import com.app.cinema.dto.hall.CreateHallDTO;
import com.app.cinema.dto.hall.HallDTO;
import com.app.cinema.dto.movie.CreateMovieDTO;
import com.app.cinema.dto.movie.MovieDTO;
import com.app.cinema.dto.reservation.CreateReservationDTO;
import com.app.cinema.dto.reservation.ReservationDTO;
import com.app.cinema.dto.screening.CreateScreeningDTO;
import com.app.cinema.dto.screening.ScreeningDTO;
import com.app.cinema.model.Seat;
import com.app.cinema.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReservationControllerTest {


    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;

    private WebClient authClient;

    @Autowired
    private SeatRepository seatRepository;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
        authClient = WebClient.builder()
                .baseUrl("http://localhost:" + port + "/api/auth")
                .build();
    }

    private String getAdminToken() {
        AuthResponse response = authClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("admin", "admin123"))
                .retrieve().bodyToMono(AuthResponse.class).block();
        return response.getToken();
    }
    private WebClientResponseException getExpectError(String path, String token) {
        try {
            webClient.get().uri(path)

                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return null;
        } catch (WebClientResponseException e) {
            return e;
        }
    }
    private String getUserToken() {
        String username = "user_" + System.currentTimeMillis();
        authClient.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RegisterRequest(username, username + "@test.com", "password123"))
                .retrieve().bodyToMono(String.class).block();
        AuthResponse response = authClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(username, "password123"))
                .retrieve().bodyToMono(AuthResponse.class).block();
        return response.getToken();
    }
    private <T> T put(String path, Object body, String token, Class<T> type) {
        return webClient.put().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(body)
                .retrieve().bodyToMono(type).block();
    }
    private <T> T get(String path, String token, Class<T> type) {
        return webClient.get().uri(path)
                .header("Authorization", "Bearer " + token)
                .retrieve().bodyToMono(type).block();
    }

    private ScreeningDTO createTestScreening(String token){
        MovieDTO TestMovie = createTestMovie(token);
        HallDTO TestHall = createTestHall(token);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(TestMovie.getId())
                .hallId(TestHall.getId())
                .startTime(LocalDate.now().plusDays(1).atTime(18, 0))
                .endTime(LocalDate.now().plusDays(1).atTime(20, 0))
                .price(new java.math.BigDecimal("25.00"))
                .build();
        return webClient.post()
                .uri("/api/screenings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createScreeningDTO)
                .retrieve()
                .bodyToMono(ScreeningDTO.class)
                .block();
    }

    private MovieDTO createTestMovie(String token) {
        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .title("Test Movie"+System.currentTimeMillis())
                .description("test")
                .durationMinutes(120)
                .releaseDate(LocalDate.now())
                .build();
        return webClient.post()
                .uri("/api/movies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createMovieDTO)
                .retrieve()
                .bodyToMono(MovieDTO.class)
                .block();
    }


    private HallDTO createTestHall(String token) {

        CreateHallDTO createHallDTO = CreateHallDTO.builder()
                .name("Test Hall"+System.currentTimeMillis())
                .rows(10)
                .seatsPerRow(15)
                .build();
        return webClient.post()
                .uri("/api/halls")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createHallDTO)
                .retrieve()
                .bodyToMono(HallDTO.class)
                .block();
    }
    private WebClientResponseException getExpectError(String path) {
        try {
            webClient.get().uri(path)
                    .retrieve().bodyToMono(String.class).block();
            return null;
        } catch (WebClientResponseException e) {
            return e;
        }
    }

    private <T> T[] getArray(String path, String token, Class<T[]> type) {
        return webClient.get().uri(path)
                .header("Authorization", "Bearer " + token)
                .retrieve().bodyToMono(type).block();
    }

    private <T> T post(String path, Object body, String token, Class<T> type) {
        return webClient.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(body)
                .retrieve().bodyToMono(type).block();
    }


    private WebClientResponseException putExpectError(String path, Object body, String token) {
        try {
            webClient.put().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(body)
                    .retrieve().bodyToMono(String.class).block();
            return null;
        } catch (WebClientResponseException e) {
            return e;
        }
    }


    private WebClientResponseException postExpectError(String path, Object body, String token) {
        try {
            webClient.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(body)
                    .retrieve().bodyToMono(String.class).block();
            return null;
        } catch (WebClientResponseException e) {
            return e;
        }
    }


    @Test
    public void createReservation_returns201() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();

        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        List<Long> seatsToBook = Arrays.asList(availableSeatIds.get(0), availableSeatIds.get(1));

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(seatsToBook)
                .build();

            ReservationDTO response = post("/api/reservations", request, userToken, ReservationDTO.class);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();

    }
    @Test
    public void createReservation_withNullScreeningId_returns400() {
        String userToken = getUserToken();


        CreateReservationDTO request = CreateReservationDTO.builder()
                .seatIds(List.of(1L, 2L))
                .build();

        WebClientResponseException error = postExpectError("/api/reservations", request, userToken);

        assertThat(error).isNotNull();

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createReservation_withEmptySeats_returns400() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of())
                .build();

        WebClientResponseException error = postExpectError("/api/reservations", request, userToken);

        assertThat(error).isNotNull();

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test
    public void cancelReservation_byOwner_changesStatusToCancelled() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();

        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of(availableSeatIds.get(0)))
                .build();

        ReservationDTO created = post("/api/reservations", request, userToken, ReservationDTO.class);
        assertThat(created.getStatus().name()).isEqualTo("PENDING");

        ReservationDTO cancelled = put("/api/reservations/" + created.getId() + "/cancel", Map.of(), userToken, ReservationDTO.class);

        assertThat(cancelled).isNotNull();
        assertThat(cancelled.getStatus().name()).isEqualTo("CANCELLED");
    }
    @Test
    public void getMyReservations_returnsList() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();

        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of(availableSeatIds.get(0)))
                .build();

        post("/api/reservations", request, userToken, ReservationDTO.class);

        ReservationDTO[] myReservations = get("/api/reservations/my", userToken, ReservationDTO[].class);

        assertThat(myReservations).isNotNull();
        assertThat(myReservations.length).isGreaterThan(0);
        assertThat(myReservations[0].getScreeningId()).isEqualTo(screening.getId());
    }
    @Test
    public void getAllReservations_byNormalUser_returns403() {
        String userToken = getUserToken();

        WebClientResponseException error = getExpectError("/api/reservations", userToken);

        assertThat(error).isNotNull();

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    @Test
    public void confirmReservation_byAdmin_changesStatusToConfirmed() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();

        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of(availableSeatIds.get(0)))
                .build();

        ReservationDTO created = post("/api/reservations", request, userToken, ReservationDTO.class);

        ReservationDTO confirmed = put("/api/reservations/" + created.getId() + "/confirm", Map.of(), adminToken, ReservationDTO.class);

        assertThat(confirmed).isNotNull();
        assertThat(confirmed.getStatus().name()).isEqualTo("CONFIRMED");
    }
    @Test
    public void confirmReservation_byNormalUser_returns403() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();
        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of(availableSeatIds.get(0)))
                .build();

        ReservationDTO created = post("/api/reservations", request, userToken, ReservationDTO.class);

        WebClientResponseException error = putExpectError("/api/reservations/" + created.getId() + "/confirm", Map.of(), userToken);

        assertThat(error).isNotNull();

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    @Test
    public void cancelReservation_byAdmin_changesStatusToCancelled() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);
        String userToken = getUserToken();

        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of(availableSeatIds.get(0)))
                .build();

        ReservationDTO created = post("/api/reservations", request, userToken, ReservationDTO.class);

        ReservationDTO cancelled = put("/api/reservations/" + created.getId() + "/cancel", Map.of(), adminToken, ReservationDTO.class);

        assertThat(cancelled).isNotNull();
        assertThat(cancelled.getStatus().name()).isEqualTo("CANCELLED");
    }
    @Test
    public void cancelReservation_byDifferentUser_returns403() {
        String adminToken = getAdminToken();
        ScreeningDTO screening = createTestScreening(adminToken);

        String userToken1 = getUserToken();
        String userToken2 = getUserToken();

        List<Long> availableSeatIds = seatRepository.findByHallId(screening.getHallId())
                .stream().map(Seat::getId).toList();

        CreateReservationDTO request = CreateReservationDTO.builder()
                .screeningId(screening.getId())
                .seatIds(List.of(availableSeatIds.get(0)))
                .build();


        ReservationDTO created = post("/api/reservations", request, userToken1, ReservationDTO.class);

        WebClientResponseException error = putExpectError("/api/reservations/" + created.getId() + "/cancel", Map.of(), userToken2);

        assertThat(error).isNotNull();

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}
