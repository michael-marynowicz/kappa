package com.company.sprintreporter.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class TopicBreakdownDto {
    private final String topic;
    private final int storyPoints;
    private final double percentage;
    private final int issueCount;
}
