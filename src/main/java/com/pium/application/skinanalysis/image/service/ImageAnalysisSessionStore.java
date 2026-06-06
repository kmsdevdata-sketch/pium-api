package com.pium.application.skinanalysis.image.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.user.vo.UserId;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Component
class ImageAnalysisSessionStore {

    private static final Duration SESSION_TTL = Duration.ofMinutes(10);
    private static final long MAX_SESSION_SIZE = 50;
    private static final int WORKER_COUNT = 2;

    private final Cache<String, ImageAnalysisSession> sessions = Caffeine.newBuilder()
            .expireAfterWrite(SESSION_TTL)
            .maximumSize(MAX_SESSION_SIZE)
            .build();

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);

    String start(UserId userId, Supplier<ImageSkinAnalysis> analysisSupplier) {
        String sessionId = UUID.randomUUID().toString();
        CompletableFuture<ImageSkinAnalysis> future = CompletableFuture.supplyAsync(analysisSupplier, executor);
        sessions.put(sessionId, new ImageAnalysisSession(sessionId, userId, future));
        return sessionId;
    }

    Optional<ImageAnalysisSession> find(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.getIfPresent(sessionId));
    }

    void remove(String sessionId) {
        sessions.invalidate(sessionId);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
