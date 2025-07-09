package org.openlake.workSync.app.service;

import org.openlake.workSync.app.dto.GithubAnalyticsDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GithubAnalyticsService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GITHUB_API = "https://api.github.com/repos/";

    public GithubAnalyticsDTO fetchAnalytics(String repoUrl) {
        // Extract owner/repo from URL
        String[] parts = repoUrl.replace("https://github.com/", "").split("/");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid GitHub repo URL");
        String owner = parts[0];
        String repo = parts[1];
        String apiUrl = GITHUB_API + owner + "/" + repo;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        // Optionally: Add GitHub token for higher rate limits
        // headers.setBearerAuth("YOUR_GITHUB_TOKEN");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> repoResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            JsonNode repoJson = objectMapper.readTree(repoResponse.getBody());

            int stars = repoJson.path("stargazers_count").asInt(0);
            int forks = repoJson.path("forks_count").asInt(0);
            int openIssues = repoJson.path("open_issues_count").asInt(0);
            int watchers = repoJson.path("subscribers_count").asInt(0);

            // Pull requests
            String pullsUrl = apiUrl + "/pulls?state=all";
            ResponseEntity<String> pullsResponse = restTemplate.exchange(pullsUrl, HttpMethod.GET, entity, String.class);
            JsonNode pullsArray = objectMapper.readTree(pullsResponse.getBody());
            int pullRequests = pullsArray.size();

            // Contributors
            String contributorsUrl = apiUrl + "/contributors";
            ResponseEntity<String> contributorsResponse = restTemplate.exchange(contributorsUrl, HttpMethod.GET, entity, String.class);
            JsonNode contributorsArray = objectMapper.readTree(contributorsResponse.getBody());
            int contributors = contributorsArray.size();

            return GithubAnalyticsDTO.builder()
                    .stars(stars)
                    .forks(forks)
                    .openIssues(openIssues)
                    .watchers(watchers)
                    .pullRequests(pullRequests)
                    .contributors(contributors)
                    .repoUrl(repoUrl)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub analytics", e);
        }
    }
}
