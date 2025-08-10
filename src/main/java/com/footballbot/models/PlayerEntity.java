package com.footballbot.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "players")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;
    private String username;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private MatchEntity match;

    public Long getId() {
        return id;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public String getUsername() {
        return username;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMatch(MatchEntity match) {
        this.match = match;
    }
}
