package com.flores.oauth2login.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;

@Service
public class GooglePeopleService {

    @Value("${google.api.credentials-path}")
    private String credentialsPath;

    public List<Person> getContacts(String accessToken) throws GeneralSecurityException, IOException {
        final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final var JSON_FACTORY = GsonFactory.getDefaultInstance();

        HttpRequestInitializer requestInitializer = request -> {
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        };

        PeopleService peopleService = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName("Google People API Java Quickstart")
                .build();

        ListConnectionsResponse response = peopleService.people().connections()
            .list("people/me")
            .setPageSize(10)
            .setPersonFields("names,emailAddresses")
            .execute();

        return response.getConnections();
    }
}