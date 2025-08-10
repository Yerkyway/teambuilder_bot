package com.footballbot.models;

import jakarta.persistence.*;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private LocalDate date;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<PlayerEntity> players = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<PlayerEntity> getPlayers() {
        return players;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setPlayers(List<PlayerEntity> players) {
        this.players = players;
    }
}
