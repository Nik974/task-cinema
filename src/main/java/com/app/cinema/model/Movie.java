package com.app.cinema.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.Duration;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "movie_title", nullable = false)
    private String title;

    @Column(name = "movie_description")
    private String description;

    @NotNull
    @Column(name = "movie_duration", nullable = false)
    private Duration duration;

    @NotNull
    @Column(name = "movie_release_date")
    private LocalDate releaseDate;

}
