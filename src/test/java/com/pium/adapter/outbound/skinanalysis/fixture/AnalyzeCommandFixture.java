package com.pium.adapter.outbound.skinanalysis.fixture;

import com.pium.application.skinanalysis.analyze.dto.AnalyzeCommand;

import java.util.List;

public class AnalyzeCommandFixture {

    public static AnalyzeCommand createAnalyzeCommand() {
        return new AnalyzeCommand(
                List.of(
                        new AnalyzeCommand.Answer("Q_DRYNESS_1", List.of("Q1_1")),
                        new AnalyzeCommand.Answer("Q_DRYNESS_2", List.of("Q2_1"))
                ),
                List.of("Q11_1", "Q11_2")
        );
    }

}
