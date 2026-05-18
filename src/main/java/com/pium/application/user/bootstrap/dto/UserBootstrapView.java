package com.pium.application.user.bootstrap.dto;

public record UserBootstrapView(
        boolean hasDiagnosis,
        EntryPoint entryPoint
) {

    public enum EntryPoint {
        SURVEY,
        HOME
    }
}
