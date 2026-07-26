package com.stockinfo.controller;

import com.stockinfo.dto.SuggestionDto;
import com.stockinfo.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<List<SuggestionDto>> getAll() {
        return ResponseEntity.ok(suggestionService.getAllSuggestions());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<SuggestionDto> getForStock(@PathVariable String symbol) {
        return ResponseEntity.ok(suggestionService.getSuggestionForStock(symbol));
    }
}
