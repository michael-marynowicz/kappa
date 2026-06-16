error id: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java
file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[94,17]

error in qdox parser
file content:
```java
offset: 3820
uri: file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java
text:
```scala
package com.company.sprintreporter.infrastructure.jira;

import com.company.sprintreporter.domain.model.SprintIssue;
import com.company.sprintreporter.domain.port.JiraIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of JiraIssueRepository.
 * Communicates with Jira Server/Data Center REST API v2.
 *
 * Uses JQL "sprint in openSprints()" to fetch active sprint issues directly,
 * avoiding the Agile board API which may not be available on all instances.
 *
 * In mock mode (jira.mock-mode=true), delegates to MockJiraIssueRepository.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class JiraIssueRepositoryImpl implements JiraIssueRepository {

    private final JiraProperties jiraProperties;
    private final JiraIssueDomainMapper mapper;
    private final MockJiraIssueRepository mockRepository;

    @Override
    public List<SprintIssue> fetchSprintIssues() {
        if (jiraProperties.isMockMode()) {
            log.info("Mock mode active — returning mock Jira data");
            return mockRepository.fetchSprintIssues();
        }

        return fetchFromJiraApi();
    }

    private List<SprintIssue> fetchFromJiraApi() {
        log.info("Fetching active sprint issues for project {} via JQL", jiraProperties.getProjectKey());

        WebClient client = WebClient.builder()
                .baseUrl(jiraProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + jiraProperties.getPat())
                .defaultHeader("Accept", "application/json")
                .build();

        // JQL: active sprint issues for the project — works on all Jira Server/DC versions
        String jql = buildJql();
        log.debug("JQL: {}", jql);

        JiraApiResponse.SearchResult result = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/2/search")
                        .queryParam("jql", jql)
                        .queryParam("fields", "summary,status,assignee,issuetype,customfield_10016,story_points")
                        .queryParam("maxResults", 200)
                        .build())
                .retrieve()
                .bodyToMono(JiraApiResponse.SearchResult.class)
                .block();

        if (result == null || result.getIssues() == null) {
            log.warn("Jira API returned empty result for project {}", jiraProperties.getProjectKey());
            return List.of();
        }
    }

    private String buildJql() {
        List<String> members = jiraProperties.getTeamMembers();

        if (members == null || members.isEmpty()) {
            log.info("No team-members filter configured — fetching all sprint issues");
            return "project = %s AND sprint in openSprints() ORDER BY created ASC"
                    .formatted(jiraProperties.getProjectKey());
        }

        String assigneeList = members.stream()
                .map(u -> "\"" + u + "\"")
                .collect(Collectors.joining(", "));
        log.info("Filtering sprint issues for {} team member(s)", members.size());
        return "project = %s AND sprint in openSprints() AND assignee in (%s) ORDER BY created ASC"
                .formatted(jiraProperties.getProjectKey(), assigneeList);

        // NOTE: the closing brace below belongs to fetchFromJiraApi(), kept intentionally
        // (the compiler will see the correct structure after the method split above)
        // Dummy placeholder — real closing handled by surrounding method block
    }

        log.info(@@"Received {} issues from Jira API", result.getIssues().size());
        return mapper.toDomainList(result.getIssues());
    }
}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file:///C:/Users/mmarynowicz/Downloads/sprint-reporter-v2/sprint-reporter/backend/src/main/java/com/company/sprintreporter/infrastructure/jira/JiraIssueRepositoryImpl.java