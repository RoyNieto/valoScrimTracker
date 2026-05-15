package com.valorant.analytics.scrimtracker.controller;

import com.valorant.analytics.scrimtracker.model.Match;
import com.valorant.analytics.scrimtracker.service.MatchService;
import com.valorant.analytics.scrimtracker.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    @Autowired
    private MatchService matchService;

    // Aquí inyectamos tu nuevo servicio de IA
    @Autowired
    private GeminiService geminiService;

    // Endpoint para guardar una partida (POST manual)
    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        Match savedMatch = matchService.saveMatch(match);
        return ResponseEntity.ok(savedMatch);
    }

    // Endpoint para obtener todas las partidas (GET)
    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    // Nuevo Endpoint para la IA: Recibe archivos en lugar de texto
    @PostMapping("/analyze")
    public ResponseEntity<Match> analyzeMatchImages(
            @RequestParam("scoreboard") MultipartFile scoreboard,
            @RequestParam("timeline") MultipartFile timeline) {
        try {
            // Mandamos las fotos a la IA
            Match analyzedMatch = geminiService.analyzeImages(scoreboard, timeline);
            
            // Devolvemos la tabla de datos estructurada
            return ResponseEntity.ok(analyzedMatch);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}