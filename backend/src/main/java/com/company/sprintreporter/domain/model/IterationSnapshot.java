package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IterationSnapshot {
    private final String sprintName;
    private final int committedStoryPoints;
    private final int deliveredStoryPoints;
    private final int velocity;
    private final double ratio;
}
