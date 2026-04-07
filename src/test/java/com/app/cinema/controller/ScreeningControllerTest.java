package com.app.cinema.controller;

import com.app.cinema.dto.auth.AuthResponse;
import com.app.cinema.dto.auth.LoginRequest;
import com.app.cinema.dto.auth.RegisterRequest;
import com.app.cinema.dto.hall.CreateHallDTO;
import com.app.cinema.dto.hall.HallDTO;
import com.app.cinema.dto.movie.CreateMovieDTO;
import com.app.cinema.dto.movie.MovieDTO;
import com.app.cinema.dto.screening.CreateScreeningDTO;
import com.app.cinema.dto.screening.ScreeningDTO;
import com.app.cinema.dto.seat.SeatDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ScreeningControllerTest {


    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;

    private WebClient authClient;

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
    public void createScreening_shouldReturnCreatedScreeningDTO() {
        String token = getAdminToken();

        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);
        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().atTime(22, 0))
                .endTime(LocalDate.now().atTime(23, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();
        ScreeningDTO screeningDTO = post("/api/screenings", createScreeningDTO, token, ScreeningDTO.class);
        assertThat(screeningDTO.getId()).isNotNull();
        assertThat(screeningDTO.getStartTime()).isEqualTo(createScreeningDTO.getStartTime());
    }

    @Test
    public void createScreening_withEmptyData_returns400() {
        String token = getAdminToken();

        createTestMovie(token);
        createTestHall(token);
        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder().build();

        assertThat(postExpectError("/api/screenings", createScreeningDTO, token).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test
    public void createScreening_withNegativePrice_returns400() {
        String token = getAdminToken();
        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().plusDays(1).atTime(18, 0))
                .endTime(LocalDate.now().plusDays(1).atTime(20, 0))
                .price(new java.math.BigDecimal("-5.00"))
                .build();

        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, token);
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createScreening_withPastStartTime_returns400() {
        String token = getAdminToken();
        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().minusDays(1).atTime(18, 0))
                .endTime(LocalDate.now().minusDays(1).atTime(20, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();

        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, token);
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test
    public void createScreening_withNonExistentMovieId_returns404() {
        String token = getAdminToken();
        HallDTO hall = createTestHall(token);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(999999L)
                .hallId(hall.getId())
                .startTime(LocalDate.now().plusDays(1).atTime(22, 0))
                .endTime(LocalDate.now().plusDays(1).atTime(23, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();

        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, token);
        assertThat(error).isNotNull();
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test
    public void createScreening_whenStartTimeEqualsEndTime_returns400() {
        String token = getAdminToken();
        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);

        LocalDateTime time = LocalDate.now().atTime(20, 0);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(time)
                .endTime(time)
                .price(new java.math.BigDecimal("20.00"))
                .build();

        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, token);

        assertThat(error).isNotNull();
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createScreening_whenStartTimeIsAfterEndTime_returns400() {
        String token = getAdminToken();
        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().atTime(22, 0))
                .endTime(LocalDate.now().atTime(20, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();

        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, token);

        assertThat(error).isNotNull();
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void createScreening_AsUser_returns403() {

        MovieDTO movie = createTestMovie(getAdminToken());
        HallDTO hall = createTestHall(getAdminToken());
        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().atTime(22, 0))
                .endTime(LocalDate.now().atTime(23, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();
        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, getUserToken());
        assertThat(error).isNotNull();
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void createScreening_withoutLogin_returns403() {

        MovieDTO movie = createTestMovie(getAdminToken());
        HallDTO hall = createTestHall(getAdminToken());
        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().atTime(22, 0))
                .endTime(LocalDate.now().atTime(23, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();
        WebClientResponseException error = postExpectError("/api/screenings", createScreeningDTO, null);
        assertThat(error).isNotNull();
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void getAllScreenings_returnsListOfScreenings() {
        String token = getAdminToken();

        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);
        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().atTime(15, 0))
                .endTime(LocalDate.now().atTime(17, 0))
                .price(new java.math.BigDecimal("20.00"))
                .build();
        post("/api/screenings", createScreeningDTO, token, ScreeningDTO.class);

        ScreeningDTO[] screenings = getArray("/api/screenings", token, ScreeningDTO[].class);

        assertThat(screenings).isNotNull();
        assertThat(screenings.length).isGreaterThan(0);

        boolean containsCreatedScreening = Arrays.stream(screenings)
                .anyMatch(s -> s.getMovieId().equals(movie.getId()) && s.getHallId().equals(hall.getId()));
        assertThat(containsCreatedScreening).isTrue();
    }

    @Test
    public void getAllScreenings_withoutToken_returns403() {
        WebClientResponseException error = getExpectError("/api/screenings");

        assertThat(error).isNotNull();

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getTakenSeats_returnsListOfSeats() {
        String token = getAdminToken();

        MovieDTO movie = createTestMovie(token);
        HallDTO hall = createTestHall(token);

        CreateScreeningDTO createScreeningDTO = CreateScreeningDTO.builder()
                .movieId(movie.getId())
                .hallId(hall.getId())
                .startTime(LocalDate.now().atTime(20, 0))
                .endTime(LocalDate.now().atTime(22, 0))
                .price(new java.math.BigDecimal("25.00"))
                .build();
        ScreeningDTO screening = post("/api/screenings", createScreeningDTO, token, ScreeningDTO.class);

        SeatDTO[] takenSeats = getArray("/api/screenings/" + screening.getId() + "/taken-seats", token, SeatDTO[].class);

        assertThat(takenSeats).isNotNull();
        assertThat(takenSeats).isEmpty();
    }
}
