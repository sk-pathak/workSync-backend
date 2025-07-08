package org.openlake.workSync.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubAnalyticsDTO {
    private int stars;
    private int forks;
    private int openIssues;
    private int watchers;
    private int pullRequests;
    private int contributors;
    private String repoUrl;
}
