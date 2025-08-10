package com.footballbot.services;

import com.footballbot.models.MatchEntity;
import com.footballbot.models.PlayerEntity;
import com.footballbot.repositories.IMatchRepository;
import com.footballbot.repositories.IPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);
    private final IMatchRepository matchRepository;
    private final IPlayerRepository playerRepository;

    public MatchService(IMatchRepository matchRepository, IPlayerRepository playerRepository) {
        this.matchRepository= matchRepository;
        this.playerRepository = playerRepository;
    }

    public Optional<MatchEntity> findMatchByDate (LocalDate date) {
        return matchRepository.findByDate(date);
    }

    public MatchEntity createMatchIfNotExists(LocalDate date) {
        Optional<MatchEntity> existingMatch = matchRepository.findByDate(date);

        if(existingMatch.isPresent()) {
            log.warn("Match for date {} already exists, returning existing match.", date);
            return null;
        }

        MatchEntity match = new MatchEntity();
        match.setDate(date);
        return matchRepository.save(match);
    }

    public List<MatchEntity> getAllMatches() {
        return matchRepository.findAll();
    }

    @Transactional
    public void addPlayer(Long telegramId, String playerName, LocalDate matchDate) {
        Optional<MatchEntity> matchOpt = matchRepository.findByDate(matchDate);

        if (!matchOpt.isPresent()) {
            throw new IllegalArgumentException("Матч на эту дату не найден: " + matchDate);
        }

        MatchEntity match = matchOpt.get();

        boolean alreadyInMatch = match.getPlayers().stream()
                .anyMatch(p -> p.getTelegramId().equals(telegramId));

        if (alreadyInMatch) {
            throw new IllegalArgumentException("Вы уже записаны на матч в эту дату" + matchDate);
        }

        PlayerEntity player = new PlayerEntity();
        player.setTelegramId(telegramId);
        player.setUsername(playerName);
        player.setMatch(match);
        match.getPlayers().add(player);
        playerRepository.save(player);
    }

    @Transactional(readOnly = true)
    public List<String> playersList(LocalDate date) {
        return matchRepository.findByDate(date)
                .map(match -> match.getPlayers().stream()
                        .map(PlayerEntity::getUsername)
                        .toList())
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<MatchEntity> getMatchesForPlayer(Long telegramId) {
        return matchRepository.findAll().stream()
                .filter(match -> match.getPlayers().stream()
                        .anyMatch(player -> player.getTelegramId().equals(telegramId)))
                .toList();
    }

    @Transactional
    public void removePlayerFromMatch(Long telegramId, LocalDate matchDate) {
        Optional<MatchEntity> matchOpt = matchRepository.findByDate(matchDate);

        if (!matchOpt.isPresent()) {
            throw new IllegalArgumentException("Матч на эту дату не найден: " + matchDate);
        }

        MatchEntity match = matchOpt.get();
        Optional<PlayerEntity> playerToRemove = match.getPlayers().stream()
                .filter(player -> player.getTelegramId().equals(telegramId))
                .findFirst();

        if (!playerToRemove.isPresent()) {
            throw new IllegalArgumentException("Вы не записаны на матч в эту дату: " + matchDate);
        }

        PlayerEntity player = playerToRemove.get();
        match.getPlayers().remove(player);
        playerRepository.delete(player);
        matchRepository.save(match);
    }
}
