package com.app.cinema.controller;

import com.app.cinema.dto.auth.LoginRequest;
import com.app.cinema.dto.auth.AuthResponse;
import com.app.cinema.dto.auth.RegisterRequest;
import com.app.cinema.dto.hall.CreateHallDTO;
import com.app.cinema.dto.hall.HallDTO;
import com.app.cinema.dto.seat.SeatDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HallControllerTest {

    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;
    private WebClient authClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port + "/api/halls")
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

    private WebClientResponseException postNoAuth(String path, Object body) {
        try {
            webClient.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve().bodyToMono(String.class).block();
            return null;
        } catch (WebClientResponseException e) {
            return e;
        }
    }

    @Test
    void getAllHalls_byAdmin_returns200() {
        String token = getAdminToken();

        HallDTO[] halls = getArray("", token, HallDTO[].class);

        assertThat(halls).isNotNull();
    }

    @Test
    void getAllHalls_withoutLogin_returns401() {
        try {
            webClient.get().uri("")
                    .retrieve().bodyToMono(String.class).block();
        } catch (WebClientResponseException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(401);
        }
    }

    @Test
    void getHallById() {
        String adminToken = getAdminToken();

        CreateHallDTO create = CreateHallDTO.builder()
                .name("TestSala_" + System.currentTimeMillis())
                .rows(5).seatsPerRow(10).build();
        HallDTO created = post("", create, adminToken, HallDTO.class);

        HallDTO result = get("/" + created.getId(), adminToken, HallDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo(created.getName());
    }

    @Test
    void getSeatsByHall_returnsSeats() {
        String adminToken = getAdminToken();

        CreateHallDTO create = CreateHallDTO.builder()
                .name("SalaMiejsca_" + System.currentTimeMillis())
                .rows(3).seatsPerRow(5).build();
        HallDTO created = post("", create, adminToken, HallDTO.class);

        SeatDTO[] seats = getArray("/" + created.getId() + "/seats", adminToken, SeatDTO[].class);

        assertThat(seats).isNotNull();
        assertThat(seats.length).isEqualTo(15);
        assertThat(seats[0].getHallId()).isEqualTo(created.getId());
    }

    @Test
    void addHall_returnsHall() {
        String adminToken = getAdminToken();
        CreateHallDTO request = CreateHallDTO.builder()
                .name("NowaSala_" + System.currentTimeMillis())
                .rows(8).seatsPerRow(12).build();

        HallDTO result = post("", request, adminToken, HallDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getRows()).isEqualTo(8);
        assertThat(result.getSeatsPerRow()).isEqualTo(12);
    }

    @Test
    void addHall_AsUser_returns403() {
        String userToken = getUserToken();
        CreateHallDTO request = CreateHallDTO.builder()
                .name("NowaSala_" + System.currentTimeMillis())
                .rows(5).seatsPerRow(10).build();

        WebClientResponseException ex = postExpectError("", request, userToken);

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void addHall_withoutLogin_returns403() {
        CreateHallDTO request = CreateHallDTO.builder()
                .name("NowaSala").rows(5).seatsPerRow(10).build();

        WebClientResponseException ex = postNoAuth("", request);

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode().value()).isEqualTo( 401);
    }

    @Test
    void addHall_emptyHallName_returns400() {
        String adminToken = getAdminToken();
        CreateHallDTO request = CreateHallDTO.builder()
                .name("").rows(5).seatsPerRow(10).build();

        WebClientResponseException ex = postExpectError("", request, adminToken);

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addHall_seatsPerRowOutOfLimit_returns400() {
        String adminToken = getAdminToken();
        CreateHallDTO request = CreateHallDTO.builder()
                .name("Sala").rows(5).seatsPerRow(1).build();

        WebClientResponseException ex = postExpectError("", request, adminToken);

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addHall_DuplicatedHallName_returns400() {
        String adminToken = getAdminToken();
        String name = "DuplikatSala_" + System.currentTimeMillis();

        post("", CreateHallDTO.builder().name(name).rows(5).seatsPerRow(10).build(),
                adminToken, HallDTO.class);

        WebClientResponseException ex = postExpectError("",
                CreateHallDTO.builder().name(name).rows(3).seatsPerRow(8).build(),
                adminToken);

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}