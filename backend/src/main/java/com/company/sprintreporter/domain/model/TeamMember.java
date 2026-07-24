package com.company.sprintreporter.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
@With
public class TeamMember {

    private final String id;
    private final String name;
    private final Role role;
    @Builder.Default
    private final double timeOverride = 1.0;

    public enum Role {
        DEV, PDA, QA, SM
    }

    public static TeamMember create(String id, String name, Role role) {
        return create(id, name, role, 1.0);
    }

    public static TeamMember create(String id, String name, Role role, double timeOverride) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team member name must not be blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("Team member role must not be null");
        }
        if (timeOverride < 0 || timeOverride > 1) {
            throw new IllegalArgumentException("Time override must be between 0 and 1");
        }
        return TeamMember.builder()
                .id(id != null ? id : java.util.UUID.randomUUID().toString())
                .name(name)
                .role(role)
                .timeOverride(timeOverride)
                .build();
    }
}
