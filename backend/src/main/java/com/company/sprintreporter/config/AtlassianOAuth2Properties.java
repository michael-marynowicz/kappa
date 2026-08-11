package com.company.sprintreporter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "atlassian.oauth2")
@Getter
@Setter
public class AtlassianOAuth2Properties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String frontendSuccessUrl = "http://localhost:4200/settings/jira?oauth=success";
    private String frontendErrorUrl = "http://localhost:4200/settings/jira?oauth=error";
}
