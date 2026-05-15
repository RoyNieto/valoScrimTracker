package com.valorant.analytics.scrimtracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "player_stats")
@Data
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. La NUEVA relación con el catálogo de jugadores
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "player_id")
    private Player player;

    @Transient
    private String playerName;
    private String agent;
    
    private int acs;
    private int kills;
    private int deaths;
    private int assists;
    private int econ;
    private int firstBloods;
    private int plants;
    private int defuses;
}