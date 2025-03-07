package com.flores.oauth2login.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    @Autowired
    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = WebClient.builder().build();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getContacts(OAuth2AuthenticationToken authenticationToken) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            authenticationToken.getAuthorizedClientRegistrationId(),
            authenticationToken.getName());

        if (client == null || client.getAccessToken() == null) {
            throw new RuntimeException("OAuth2AuthorizedClient is NULL or Token is missing!");
        }

        Instant now = Instant.now();
        Instant tokenExpiry = client.getAccessToken().getExpiresAt();

        if (tokenExpiry == null || tokenExpiry.isBefore(now)) {
            throw new RuntimeException("Access Token is expired!");
        }

        String accessToken = client.getAccessToken().getTokenValue();
        String url = "https://people.googleapis.com/v1/people/me/connections" + 
                    "?personFields=names,emailAddresses" +
                    "&pageSize=100";    
                    
        Map<String, Object> response = webClient.get()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)  // Get as a generic Map
            .block();

        return (List<Map<String, Object>>) response.get("connections");
    }
}
