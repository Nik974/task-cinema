package com.app.cinema.controller;

import com.app.cinema.dto.auth.AuthResponse;
import com.app.cinema.dto.auth.LoginRequest;
import com.app.cinema.dto.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @Value("${local.server.port}")
    private int port;

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port + "/api/auth")
                .build();
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    private WebClientResponseException postExpectError(String path, Object body) {
        try {
            webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return null;
        } catch (WebClientResponseException e) {
            return e;
        }
    }

    @Test
    void register_validData_returns201() {
        RegisterRequest request = new RegisterRequest(
                "newuser_" + System.currentTimeMillis(),
                "new_" + System.currentTimeMillis() + "@test.com",
                "password123"
        );

        String response = post("/register", request, String.class);
        assertThat(response).isEqualTo("Rejestracja zakończona sukcesem");
    }

    @Test
    void register_usernameTooShort_returns400() {
        RegisterRequest request = new RegisterRequest("ab", "ok@test.com", "password123");

        WebClientResponseException ex = postExpectError("/register", request);
        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_invalidEmail_returns400() {
        RegisterRequest request = new RegisterRequest("validuser", "not-an-email", "password123");

        WebClientResponseException ex = postExpectError("/register", request);
        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_passwordTooShort_returns400() {
        RegisterRequest request = new RegisterRequest("validuser", "valid@test.com", "short");

        WebClientResponseException ex = postExpectError("/register", request);
        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_duplicateUsername_returns400() {
        String username = "duplikat_" + System.currentTimeMillis();
        post("/register", new RegisterRequest(username, "first@test.com", "password123"), String.class);

        WebClientResponseException ex = postExpectError("/register",
                new RegisterRequest(username, "second@test.com", "password123"));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getResponseBodyAsString()).contains("zajęta");
    }

    @Test
    void register_duplicateEmail_returns400() {
        String email = "dup_" + System.currentTimeMillis() + "@test.com";
        post("/register", new RegisterRequest("user1_" + System.currentTimeMillis(), email, "password123"), String.class);

        WebClientResponseException ex = postExpectError("/register",
                new RegisterRequest("user2_" + System.currentTimeMillis(), email, "password123"));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getResponseBodyAsString()).contains("zajęty");
    }

    @Test
    void login_validCredentials_returns200WithToken() {
        String username = "loginuser_" + System.currentTimeMillis();
        post("/register", new RegisterRequest(username, username + "@test.com", "password123"), String.class);

        AuthResponse response = post("/login", new LoginRequest(username, "password123"), AuthResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void login_wrongPassword_returns401() {
        WebClientResponseException ex = postExpectError("/login",
                new LoginRequest("nieistniejacy", "zlehaslo"));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_blankUsername_returns400() {
        WebClientResponseException ex = postExpectError("/login",
                new LoginRequest("", "password123"));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_blankPassword_returns400() {
        WebClientResponseException ex = postExpectError("/login",
                new LoginRequest("user1", ""));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}