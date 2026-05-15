package com.valorant.analytics.scrimtracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Data
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mapName;
    private LocalDateTime date;
    
    private int roundsWonAtk;
    private int roundsWonDef;
    private boolean wonPistolAtk;
    private boolean wonPistolDef;
    private int totalRounds;

    // match 1:N player
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "match_id")
    private List<PlayerStats> playerStats;
}