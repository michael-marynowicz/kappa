package com.company.sprintreporter.application.mapper;

import com.company.sprintreporter.application.dto.SprintMetricsResponseDto;
import com.company.sprintreporter.domain.model.SprintMetrics;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper: SprintMetrics domain model → SprintMetricsResponseDto.
 * All fields map 1:1 by name so no explicit @Mapping annotations needed.
 */
@Mapper(componentModel = "spring")
public interface SprintMetricsMapper {

    SprintMetricsResponseDto toResponseDto(SprintMetrics metrics);
}
