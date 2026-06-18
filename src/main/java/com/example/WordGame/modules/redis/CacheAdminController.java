package com.example.WordGame.modules.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/cache", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class CacheAdminController {

    private final CacheManager cacheManager;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Object> cacheDetails = new HashMap<>();
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheDetails.put(cacheName, "Configured and active");
        });

        stats.put("caches", cacheDetails);
        stats.put("totalCaches", cacheManager.getCacheNames().size());
        stats.put("status", "Redis caching is active");
        stats.put("timestamp", System.currentTimeMillis());

        log.info("Cache stats requested - Total caches: {}", cacheManager.getCacheNames().size());
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        log.info("🗑️ Clearing all caches");

        long clearedCount = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).clear();
            clearedCount++;
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        response.put("clearedCachesCount", String.valueOf(clearedCount));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        log.info("🗑️ Clearing cache: {}", cacheName);

        CacheManager cacheManager = this.cacheManager;
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}