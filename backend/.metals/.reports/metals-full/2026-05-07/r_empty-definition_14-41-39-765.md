error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/application/mapper/SprintIssueMapper.java:com/company/sprintreporter/domain/model/SprintIssue#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/application/mapper/SprintIssueMapper.java
empty definition using pc, found symbol in pc: com/company/sprintreporter/domain/model/SprintIssue#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 177
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/application/mapper/SprintIssueMapper.java
text:
```scala
package com.company.sprintreporter.application.mapper;

import com.company.sprintreporter.application.dto.SprintIssueResponseDto;
import com.company.sprintreporter.domain.model.@@SprintIssue;
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
    SprintIssueResponseDto toResponseDto(SprintIssue issue);

    List<SprintIssueResponseDto> toResponseDtoList(List<SprintIssue> issues);
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: com/company/sprintreporter/domain/model/SprintIssue#