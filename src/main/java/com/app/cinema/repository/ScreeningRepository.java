package com.app.cinema.repository;

import com.app.cinema.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findByMovieId(Long movieId);

    List<Screening> findByMovieIdAndStartTimeAfter(Long movieId, LocalDateTime now);

    List<Screening> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);
}
