package com.company.sprintreporter.application.mapper;

import com.company.sprintreporter.application.dto.SprintIssueResponseDto;
import com.company.sprintreporter.domain.model.SprintIssue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper: converts domain models to response DTOs.
 * Keeps controllers and services clean — no manual field mapping.
 *
 * doneStoryPoints is computed by the domain model itself,
 * so we map it via the getter.
 */
@Mapper(componentModel = "spring")
public interface SprintIssueMapper {

    @Mapping(target = "doneStoryPoints", expression = "java(issue.getDoneStoryPoints())")
    @Mapping(target = "remainingStoryPoints", expression = "java(issue.getDisplayedRemainingStoryPoints())")
    SprintIssueResponseDto toResponseDto(SprintIssue issue);

    List<SprintIssueResponseDto> toResponseDtoList(List<SprintIssue> issues);
}
