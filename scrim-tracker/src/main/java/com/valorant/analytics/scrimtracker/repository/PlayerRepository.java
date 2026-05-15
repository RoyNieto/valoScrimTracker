package com.valorant.analytics.scrimtracker.repository;

import com.valorant.analytics.scrimtracker.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Magia de Spring: Esto crea automáticamente una consulta SQL: SELECT * FROM players WHERE name = ?
    Optional<Player> findByName(String name);
}