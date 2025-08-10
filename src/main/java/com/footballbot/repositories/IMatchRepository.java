package com.footballbot.repositories;

import com.footballbot.models.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IMatchRepository extends JpaRepository<MatchEntity, Long> {
    Optional<MatchEntity> findByDate(LocalDate date);
}
