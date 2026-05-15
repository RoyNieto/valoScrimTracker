package com.valorant.analytics.scrimtracker.service;

import com.valorant.analytics.scrimtracker.model.Match;
import com.valorant.analytics.scrimtracker.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    // Guardar una partida completa (con sus estadísticas de jugadores)
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    // Obtener todas las partidas para el Dashboard
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    // Buscar una partida por ID
    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }
}