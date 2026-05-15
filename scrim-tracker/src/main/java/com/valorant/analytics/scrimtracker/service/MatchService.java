package com.valorant.analytics.scrimtracker.service;

import com.valorant.analytics.scrimtracker.model.Match;
import com.valorant.analytics.scrimtracker.model.Player;
import com.valorant.analytics.scrimtracker.model.PlayerStats;
import com.valorant.analytics.scrimtracker.repository.MatchRepository;
import com.valorant.analytics.scrimtracker.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    // Inyectamos el nuevo repositorio
    @Autowired
    private PlayerRepository playerRepository;

    public Match saveMatch(Match match) {
        
        // Antes de guardar, iteramos sobre cada jugador extraído por la IA
        for (PlayerStats stats : match.getPlayerStats()) {
            
            // Lógica "Find or Create"
            Player player = playerRepository.findByName(stats.getPlayerName())
                    .orElseGet(() -> {
                        // Si no existe, creamos uno nuevo
                        Player newPlayer = new Player();
                        newPlayer.setName(stats.getPlayerName());
                        return playerRepository.save(newPlayer);
                    });
            
            // Enlazamos las estadísticas de esta partida con el perfil histórico del jugador
            stats.setPlayer(player);
        }

        // Ahora sí, guardamos la partida completa
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }
}