package com.company.sprintreporter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "metrics")
public class MetricsProperties {

    private Capacity capacity = new Capacity();
    private TeamAvailability teamAvailability = new TeamAvailability();

    @Getter
    @Setter
    public static class Capacity {
        private double planned = 0;
        private double real = 0;
    }

    @Getter
    @Setter
    public static class TeamAvailability {
        private double dev = 0;
        private double pda = 0;
        private double qa = 0;
    }
}
