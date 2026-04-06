package com.app.cinema.controller;

import com.app.cinema.dto.auth.AuthResponse;
import com.app.cinema.dto.auth.LoginRequest;
import com.app.cinema.dto.auth.RegisterRequest;
import com.app.cinema.dto.movie.CreateMovieDTO;
import com.app.cinema.dto.movie.EditMovieDTO;
import com.app.cinema.dto.movie.MovieDTO;
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
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MovieControllerTest {

    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;

    private WebClient authClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port + "/api/movies")
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


    private <T> T get(String path, String token, Class<T> type) {
        return webClient.get().uri(path)
                .header("Authorization", "Bearer " + token)
                .retrieve().bodyToMono(type).block();
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

    private <T> T put(String path, Object body, String token, Class<T> type) {
        return webClient.put().uri(path)
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
    void addMovie_shouldReturn201() {
        String token = getAdminToken();
        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .title("Test-Movie")
                .description("")
                .durationMinutes(55)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();
        MovieDTO movieDTO = post("", createMovieDTO, token, MovieDTO.class);
        MovieDTO result = get("/" + movieDTO.getId(), token, MovieDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test-Movie");
        assertThat(result.getDurationMinutes()).isEqualTo(55);
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void addMovieWithDuplicatedTitle_shouldReturn400() {
        String token = getAdminToken();


        CreateMovieDTO firstMovie = CreateMovieDTO.builder()
                .title("Duplicate-Movie")
                .durationMinutes(55)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();
        post("", firstMovie, token, MovieDTO.class);

        CreateMovieDTO duplicateMovie = CreateMovieDTO.builder()
                .title("Duplicate-Movie")
                .durationMinutes(44)
                .releaseDate(LocalDate.of(2002, 2, 2))
                .build();
        WebClientResponseException e = postExpectError("", duplicateMovie, token);

        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addMovie_withUserToken_shouldReturn403() {

        String token = getUserToken();
        WebClientResponseException e = postExpectError("", CreateMovieDTO.builder()
                .title("Test-Movie")
                .description("test")
                .durationMinutes(44)
                .releaseDate(LocalDate.of(2002, 2, 2))
                .build(), token);

        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.valueOf(403));

    }

    @Test
    void addMovieWithEmptyTitle_shouldReturn400() {
        String token = getAdminToken();
        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .title("")
                .description("")
                .durationMinutes(70)
                .releaseDate(LocalDate.of(2002, 2, 2))
                .build();
        WebClientResponseException e = postExpectError("", createMovieDTO, token);
        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    void addMovieWithEmptyData_shouldReturn400() {
        String token = getAdminToken();
        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .build();
        WebClientResponseException e = postExpectError("", createMovieDTO, token);
        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }


    @Test
    void getAllMovies_shouldReturn200() {
        String token = getAdminToken();

        CreateMovieDTO testMovie = CreateMovieDTO.builder()
                .title("List-Test-Movie")
                .description("Test description")
                .durationMinutes(120)
                .releaseDate(LocalDate.of(2023, 1, 1))
                .build();
        post("", testMovie, token, MovieDTO.class);

        MovieDTO[] movies = getArray("", token, MovieDTO[].class);

        assertThat(movies).isNotNull();
        assertThat(movies.length).isGreaterThan(0);

        boolean containsOurMovie = Arrays.stream(movies)
                .anyMatch(m -> m.getTitle().equals("List-Test-Movie"));
        assertThat(containsOurMovie).isTrue();
    }
    @Test
    void getAllMovies_byUser_shouldReturn200() {
        String token = getUserToken();

        MovieDTO[] movies= getArray("", token, MovieDTO[].class);
        assertThat(movies).isNotNull();


    }
    @Test
    void editMovie_asAdmin_returns200AndUpdatedData() {
        String adminToken = getAdminToken();

        CreateMovieDTO createMovieDTO = CreateMovieDTO.builder()
                .title("Original Title")
                .description("Original Description")
                .durationMinutes(100)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .build();
        MovieDTO savedMovie = post("", createMovieDTO, adminToken, MovieDTO.class);

        EditMovieDTO editMovieDTO = EditMovieDTO.builder()
                .title("Updated Title")
                .description("Updated Description")
                .durationMinutes(150)
                .releaseDate(LocalDate.of(2025, 12, 31))
                .build();

        MovieDTO updatedMovie = put("/" + savedMovie.getId(), editMovieDTO, adminToken, MovieDTO.class);

        assertThat(updatedMovie).isNotNull();
        assertThat(updatedMovie.getId()).isEqualTo(savedMovie.getId());
        assertThat(updatedMovie.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedMovie.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedMovie.getDurationMinutes()).isEqualTo(150);
        assertThat(updatedMovie.getReleaseDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }
    @Test
    void editMovie_asUser_returns403() {
        String userToken = getUserToken();
        String adminToken = getAdminToken();

        MovieDTO savedMovie = post("", CreateMovieDTO.builder()
                .title("User Edit Test")
                .durationMinutes(120)
                .releaseDate(LocalDate.now())
                .build(), adminToken, MovieDTO.class);

        EditMovieDTO editDTO = EditMovieDTO.builder()
                .title("Hacked Title")
                .durationMinutes(90)
                .releaseDate(LocalDate.now())
                .build();

        WebClientResponseException e = putExpectError("/" + savedMovie.getId(), editDTO, userToken);

        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void editMovie_withInvalidData_returns400() {
        String adminToken = getAdminToken();
        MovieDTO savedMovie = post("", CreateMovieDTO.builder()
                .title("Validation Test")
                .durationMinutes(120)
                .releaseDate(LocalDate.now())
                .build(), adminToken, MovieDTO.class);

        EditMovieDTO invalidEditDTO = EditMovieDTO.builder()
                .title("")
                .durationMinutes(-10)
                .releaseDate(LocalDate.now())
                .build();

        WebClientResponseException e = putExpectError("/" + savedMovie.getId(), invalidEditDTO, adminToken);

        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void editMovie_nonExistentId_returns404() {
        String adminToken = getAdminToken();

        EditMovieDTO editDTO = EditMovieDTO.builder()
                .title("Ghost Movie")
                .durationMinutes(100)
                .releaseDate(LocalDate.now())
                .build();

        WebClientResponseException e = putExpectError("/999999", editDTO, adminToken);

        assertThat(e).isNotNull();
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


}
