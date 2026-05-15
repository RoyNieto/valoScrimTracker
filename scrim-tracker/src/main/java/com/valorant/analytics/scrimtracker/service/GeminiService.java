package com.valorant.analytics.scrimtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorant.analytics.scrimtracker.model.Match;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Match analyzeImages(MultipartFile scoreboard, MultipartFile performance, MultipartFile timeline) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // 1. Convertir las TRES imágenes a Base64
        String scoreboardB64 = Base64.getEncoder().encodeToString(scoreboard.getBytes());
        String performanceB64 = Base64.getEncoder().encodeToString(performance.getBytes());
        String timelineB64 = Base64.getEncoder().encodeToString(timeline.getBytes());

        // 2. El NUEVO Prompt Maestro
        String prompt = """
        Eres un analista de datos experto en eSports, específicamente en VALORANT.
        Tu tarea es extraer métricas exactas de tres capturas de pantalla de una partida y devolverlas en un formato estructurado.

        INSTRUCCIONES POR IMAGEN:
        1. Scoreboard: La imagen muestra la tabla agrupada por equipos. Extrae SÓLO las estadísticas individuales de los primeros 5 jugadores de la lista (el equipo superior). Ignora a los 5 jugadores del fondo. Captura el Nombre, Agente, ACS, KDA (Kills, Deaths, Assists), Econ, First Bloods, Plants y Defuses.
        2. Performance (Match Highlights): Localiza la tabla de la derecha. Extrae las estadísticas globales del equipo divididas por ATK (Ataque) y DEF (Defensa). Busca los valores exactos para: Rondas ganadas, First bloods, First blood wins, Elimination wins, Spikes deployed, Post-spike wins, Defusals, Def team eliminated y Detonations.
        3. Timeline: Analiza la línea de tiempo de rondas. Determina si el equipo ganó la Ronda 1 (Pistolas Ataque o Defensa dependiendo del lado inicial) y la ronda inmediatamente posterior al cambio de lado (usualmente la Ronda 13, marcada después del ícono de flechas circulares).

        REGLAS DE SALIDA (CRÍTICO):
        - Tu única respuesta debe ser un objeto JSON puro.
        - NO incluyas formato markdown (como ```json).
        - NO incluyas saludos ni explicaciones.
        - Utiliza EXACTAMENTE esta estructura y nombres de llaves:

        {
          "mapName": "NombreDelMapa",
          "totalRounds": 0,
          "wonPistolAtk": true,
          "wonPistolDef": false,
          "roundsWonAtk": 0,
          "roundsWonDef": 0,
          "firstBloodsAtk": 0,
          "firstBloodsDef": 0,
          "firstBloodWinsAtk": 0,
          "firstBloodWinsDef": 0,
          "eliminationWinsAtk": 0,
          "eliminationWinsDef": 0,
          "spikesDeployedAtk": 0,
          "spikesDeployedDef": 0,
          "postSpikeWinsAtk": 0,
          "postSpikeWinsDef": 0,
          "defusalsAtk": 0,
          "defusalsDef": 0,
          "defTeamEliminatedAtk": 0,
          "defTeamEliminatedDef": 0,
          "detonationsAtk": 0,
          "detonationsDef": 0,
          "playerStats": [
            {
              "playerName": "Nombre",
              "agent": "Agente",
              "acs": 0,
              "kills": 0,
              "deaths": 0,
              "assists": 0,
              "econ": 0,
              "firstBloods": 0,
              "plants": 0,
              "defuses": 0
            }
          ]
        }
        """;
        // 3. Armar el paquete
        Map<String, Object> requestBody = createGeminiPayload(prompt, scoreboardB64, performanceB64, timelineB64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String responseRaw = restTemplate.postForObject(url, request, String.class);

        JsonNode root = objectMapper.readTree(responseRaw);
        String textResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        String cleanJson = textResponse.replace("```json", "").replace("```", "").trim();

        return objectMapper.readValue(cleanJson, Match.class);
    }

private Map<String, Object> createGeminiPayload(String prompt, String img1, String img2, String img3) {
        List<Map<String, Object>> parts = Arrays.asList(
                Map.of("text", prompt),
                Map.of("inline_data", Map.of("mime_type", "image/jpeg", "data", img1)),
                Map.of("inline_data", Map.of("mime_type", "image/jpeg", "data", img2)),
                Map.of("inline_data", Map.of("mime_type", "image/jpeg", "data", img3))
        );
        Map<String, Object> content = Map.of("parts", parts);
        return Map.of("contents", List.of(content));
    }
}