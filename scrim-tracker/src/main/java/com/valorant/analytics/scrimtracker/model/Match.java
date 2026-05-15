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
    private int totalRounds;
    
    private boolean wonPistolAtk;
    private boolean wonPistolDef;

    private int roundsWonAtk;
    private int roundsWonDef;
    private int firstBloodsAtk;
    private int firstBloodsDef;
    private int firstBloodWinsAtk;
    private int firstBloodWinsDef;
    private int eliminationWinsAtk;
    private int eliminationWinsDef;
    private int spikesDeployedAtk;
    private int spikesDeployedDef;
    private int postSpikeWinsAtk;
    private int postSpikeWinsDef;
    private int defusalsAtk;
    private int defusalsDef;
    private int defTeamEliminatedAtk;
    private int defTeamEliminatedDef;
    private int detonationsAtk;
    private int detonationsDef;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "match_id")
    private List<PlayerStats> playerStats;

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }
}