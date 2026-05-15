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

    public Match analyzeImages(MultipartFile scoreboard, MultipartFile timeline) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // 1. Convertir los archivos reales a Base64 para Google
        String scoreboardB64 = Base64.getEncoder().encodeToString(scoreboard.getBytes());
        String timelineB64 = Base64.getEncoder().encodeToString(timeline.getBytes());

        // 2. Nuestro Prompt Maestro estructurado
        String prompt = "Analiza estas dos imágenes de una scrim de VALORANT (Scoreboard y summary). El formato puede ser ALL ROUNDS (24 rondas que un equipo puede tener mas de 13 aunque al punto 13 se gane). Extrae las estadísticas de los jugadores del equipo (fondo verde)y el rendimiento del equipo. Devuelve ÚNICAMENTE un objeto JSON con este formato exacto: {\"mapName\": \"NombreMapa\", \"roundsWonAtk\": 0, \"roundsWonDef\": 0, \"wonPistolAtk\": true, \"wonPistolDef\": false, \"totalRounds\": 24, \"playerStats\": [{\"playerName\": \"Nombre\", \"agent\": \"Agente\", \"acs\": 0, \"kills\": 0, \"deaths\": 0, \"assists\": 0, \"econ\": 0, \"firstBloods\": 0, \"plants\": 0, \"defuses\": 0}]}. No incluyas comillas invertidas ni texto de código markdown, solo el JSON puro.";

        // 3. Armar el "paquete" (Payload) para Gemini
        Map<String, Object> requestBody = createGeminiPayload(prompt, scoreboardB64, timelineB64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 4. Hacer la llamada HTTP a Google
        String responseRaw = restTemplate.postForObject(url, request, String.class);

        // 5. Extraer el texto de la respuesta y limpiarlo
        JsonNode root = objectMapper.readTree(responseRaw);
        String textResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        String cleanJson = textResponse.replace("```json", "").replace("```", "").trim();

        // 6. Convertir el JSON de la IA en nuestra clase Java
        return objectMapper.readValue(cleanJson, Match.class);
    }

    // Método auxiliar para estructurar el JSON que pide Google
    private Map<String, Object> createGeminiPayload(String prompt, String img1, String img2) {
        List<Map<String, Object>> parts = Arrays.asList(
                Map.of("text", prompt),
                Map.of("inline_data", Map.of("mime_type", "image/jpeg", "data", img1)),
                Map.of("inline_data", Map.of("mime_type", "image/jpeg", "data", img2))
        );
        Map<String, Object> content = Map.of("parts", parts);
        return Map.of("contents", List.of(content));
    }
}