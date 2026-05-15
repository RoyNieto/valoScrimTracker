package com.valorant.analytics.scrimtracker.repository;

import com.valorant.analytics.scrimtracker.model.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {

}