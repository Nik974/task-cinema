package com.app.cinema.repository;

import com.app.cinema.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);
    boolean existsByTitle(String title);
    Optional<Movie> findById(Long aLong);

    List<Movie> findAll();
}
