package com.pium.application.user.bootstrap.dto;

public record UserBootstrapView(
        String userName,
        boolean hasDiagnosis,
        EntryPoint entryPoint
) {

    public enum EntryPoint {
        SURVEY,
        HOME
    }
}
