package com.pium.application.skinanalysis.image.service;

import com.pium.application.skinanalysis.image.required.dto.ImageSkinAnalysis;
import com.pium.domain.user.vo.UserId;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

class ImageAnalysisSession {

    private final String sessionId;
    private final UserId userId;
    private final CompletableFuture<ImageSkinAnalysis> future;
    private final AtomicBoolean consumed = new AtomicBoolean(false);

    ImageAnalysisSession(
            String sessionId,
            UserId userId,
            CompletableFuture<ImageSkinAnalysis> future
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.future = future;
    }

    String sessionId() {
        return sessionId;
    }

    boolean belongsTo(UserId userId) {
        return this.userId.equals(userId);
    }

    boolean isDone() {
        return future.isDone();
    }

    ImageSkinAnalysis join() {
        return future.join();
    }

    boolean markConsumed() {
        return consumed.compareAndSet(false, true);
    }
}
