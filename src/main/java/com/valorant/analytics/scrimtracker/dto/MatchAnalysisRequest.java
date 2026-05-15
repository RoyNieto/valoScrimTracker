package com.valorant.analytics.scrimtracker.dto;

import lombok.Data;

@Data
public class MatchAnalysisRequest {
    private String scoreboardImageBase64;
    private String timelineImageBase64;
}