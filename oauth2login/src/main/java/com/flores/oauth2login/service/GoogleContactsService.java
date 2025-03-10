package com.flores.oauth2login.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

@Service
public class GoogleContactsService {
    public PeopleService getPeopleService(OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential credential = new GoogleCredential()
                .setAccessToken(authorizedClient.getAccessToken().getTokenValue());

        return new PeopleService.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Google Contacts Integration")
                .build();
    }

    public List<Person> getContacts(OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(authorizedClient);

        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPageSize(100)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        if (response.getConnections() == null) {
            return new ArrayList<>();
        }

        // Ensure all contacts have at least an empty list for names, emails, and phones
        return response.getConnections().stream()
                .map(contact -> {
                    if (contact.getNames() == null) {
                        contact.setNames(new ArrayList<>()); // Prevent Thymeleaf errors
                    }
                    if (contact.getEmailAddresses() == null) {
                        contact.setEmailAddresses(new ArrayList<>());
                    }
                    if (contact.getPhoneNumbers() == null) {
                        contact.setPhoneNumbers(new ArrayList<>());
                    }
                    return contact;
                })
                .collect(Collectors.toList());
    }


    public Person getContactByResourceName(OAuth2AuthorizedClient authorizedClient, String resourceName) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(authorizedClient);

        return peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public Person createContact(OAuth2AuthorizedClient authorizedClient, Person contact) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(authorizedClient);

        return peopleService.people().createContact(contact)
                .setFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public Person updateContact(OAuth2AuthorizedClient authorizedClient, String resourceName, String givenName, String familyName, String email, String phoneNumber) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(authorizedClient);

        Person updateContact = peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        String etag = updateContact.getEtag();

        List<Name> names = new ArrayList<>();
        names.add(new Name().setGivenName(givenName).setFamilyName(familyName));

        List<EmailAddress> emailAddresses = new ArrayList<>();
        if (email != null && !email.isEmpty()) {
            emailAddresses.add(new EmailAddress().setValue(email));
        }

        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            phoneNumbers.add(new PhoneNumber().setValue(phoneNumber));
        }

        Person updatedContact = new Person();
        updatedContact.setEtag(etag);
        updatedContact.setNames(names);
        updatedContact.setEmailAddresses(emailAddresses);
        updatedContact.setPhoneNumbers(phoneNumbers);

        return peopleService.people().updateContact(resourceName, updatedContact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }


    public void deleteContact(OAuth2AuthorizedClient authorizedClient, String resourceName) throws GeneralSecurityException, IOException {
        PeopleService peopleService = getPeopleService(authorizedClient);

        peopleService.people().deleteContact(resourceName).execute();
    }
}
