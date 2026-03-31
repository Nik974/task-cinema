package com.app.cinema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Entity
@Table(name = "halls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hall_id", nullable = false)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "hall_name", nullable = false, unique = true, length = 50)
    private String name;

    @Min(1)
    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Min(1)
    @Column(name = "seats_per_row", nullable = false)
    private int seatsPerRow;


}
