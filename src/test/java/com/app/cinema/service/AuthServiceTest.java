package com.app.cinema.service;

import com.app.cinema.model.Role;
import com.app.cinema.dto.auth.AuthResponse;
import com.app.cinema.dto.auth.LoginRequest;
import com.app.cinema.dto.auth.RegisterRequest;
import com.app.cinema.model.User;
import com.app.cinema.repository.UserRepository;
import com.app.cinema.security.JwtTokenProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("jankowalski")
                .email("jan@example.com")
                .passwordHash("hashed_password")
                .role(Role.USER)
                .build();
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("jankowalski", "password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken("jankowalski", "USER"))
                .thenReturn("mocked.jwt.token");

        AuthResponse result = authService.login(request);

        assertThat(result.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.getUsername()).isEqualTo("jankowalski");
        assertThat(result.getRole()).isEqualTo("USER");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken("jankowalski", "USER");
    }

    @Test
    void login_ShouldPassCorrectCredentialsToAuthenticationManager() {
        LoginRequest request = new LoginRequest("jankowalski", "password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any(), any())).thenReturn("token");

        authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        assertThat(captor.getValue().getPrincipal()).isEqualTo("jankowalski");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password123");
    }

    @Test
    void login_ShouldThrow_WhenAuthenticationFails() {
        LoginRequest request = new LoginRequest("jankowalski", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Złe dane logowania"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void register_ShouldSaveUser_WhenDataIsValid() {
        RegisterRequest request = new RegisterRequest("jankowalski",
                "jan@example.com", "password123");

        when(userRepository.existsByUsername("jankowalski")).thenReturn(false);
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("jankowalski");
        assertThat(saved.getEmail()).isEqualTo("jan@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed_password");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_ShouldThrow_WhenUsernameAlreadyTaken() {
        RegisterRequest request = new RegisterRequest("jankowalski",
                "jan@example.com", "password123");

        when(userRepository.existsByUsername("jankowalski")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("użytkownika");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_ShouldThrow_WhenEmailAlreadyTaken() {
        RegisterRequest request = new RegisterRequest("jankowalski",
                "jan@example.com", "password123");

        when(userRepository.existsByUsername("jankowalski")).thenReturn(false);
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_ShouldEncodePassword_BeforeSaving() {
        RegisterRequest request = new RegisterRequest("jankowalski",
                "jan@example.com", "plaintext");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$encoded");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash())
                .isNotEqualTo("plaintext")
                .isEqualTo("$2a$encoded");
    }

    @Test
    void register_ShouldAlwaysAssignUserRole() {
        RegisterRequest request = new RegisterRequest("nowyuser",
                "nowy@example.com", "password123");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
    }
}