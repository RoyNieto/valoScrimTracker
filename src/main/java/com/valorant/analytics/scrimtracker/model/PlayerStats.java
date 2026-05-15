package com.valorant.analytics.scrimtracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "player_stats")
@Data // LOMBOK AUTO GET & SET
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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