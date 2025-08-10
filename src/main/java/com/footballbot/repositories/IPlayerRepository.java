package com.footballbot.repositories;

import com.footballbot.models.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPlayerRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByTelegramId(Long telegramId);
}
