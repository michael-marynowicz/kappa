error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/config/WebConfig.java:_empty_/CorsRegistry#addMapping#allowedOrigins#allowedMethods#allowedHeaders#allowCredentials#
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/config/WebConfig.java
empty definition using pc, found symbol in pc: _empty_/CorsRegistry#addMapping#allowedOrigins#allowedMethods#allowedHeaders#allowCredentials#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 925
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/config/WebConfig.java
text:
```scala
package com.company.sprintreporter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * Enables CORS for the Angular frontend dev server.
 * In production, restrict origins to your actual frontend domain.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:4200",   // Angular dev server
                        "http://localhost:80"       // Potential production deployment
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .@@allowCredentials(false)
                .maxAge(3600);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/CorsRegistry#addMapping#allowedOrigins#allowedMethods#allowedHeaders#allowCredentials#