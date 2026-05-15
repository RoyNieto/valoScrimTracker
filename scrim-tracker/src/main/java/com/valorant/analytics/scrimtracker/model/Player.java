package com.valorant.analytics.scrimtracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "players")
@Data
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aquí guardaremos su tag único
    @Column(unique = true)
    private String name;
}