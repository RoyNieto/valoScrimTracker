package com.valorant.analytics.scrimtracker.repository;

import com.valorant.analytics.scrimtracker.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

}