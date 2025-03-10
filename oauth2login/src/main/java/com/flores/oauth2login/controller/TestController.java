package com.flores.oauth2login.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.flores.oauth2login.service.GoogleContactsService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

@Controller
public class TestController {
    @Autowired
    GoogleContactsService googleContactsService;

    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/";
        }

        model.addAttribute("user", principal.getAttributes());
        return "user-info";
    }

    @GetMapping("/contacts")
    public String getContacts(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                            @AuthenticationPrincipal OAuth2User oauth2User,
                            Model model) throws GeneralSecurityException, IOException {
        List<Person> contacts = googleContactsService.getContacts(authorizedClient);

        // Ensure contacts is never null
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        model.addAttribute("contacts", contacts);
        model.addAttribute("user", oauth2User.getAttributes());
        return "contacts";
    }


    @PostMapping("/create")
    public String createContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                @RequestParam String givenName,
                                @RequestParam String familyName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phoneNumber) throws GeneralSecurityException, IOException {
        Person newContact = new Person();

        Name name = new Name();// set each manually kai maguba
        name.setGivenName(givenName);
        name.setFamilyName(familyName);
        newContact.setNames(List.of(name));

        if (email != null && !email.isEmpty()) {
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.setValue(email);
            newContact.setEmailAddresses(List.of(emailAddress));
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            PhoneNumber phone = new PhoneNumber();
            phone.setValue(phoneNumber);
            newContact.setPhoneNumbers(List.of(phone));
        }
        googleContactsService.createContact(authorizedClient, newContact);
        return "redirect:/contacts";
    }

    @GetMapping("/edit")
    public String findContactByResourceName(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                            @RequestParam String resourceName,
                                            Model model) throws GeneralSecurityException, IOException {

        Person contact = googleContactsService.getContactByResourceName(authorizedClient, resourceName);

        System.out.println("Fetched Contact: " + (contact != null ? contact.toPrettyString() : "No Contact Found"));
        System.out.println("Resource Name: " + resourceName);

        model.addAttribute("contact", contact);
        model.addAttribute("resourceName", resourceName);
        return "edit-contact";
    }

    @PostMapping("/edit/submit")
    public String submitContactEdit(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                    @RequestParam String resourceName,
                                    @RequestParam String givenName,
                                    @RequestParam String familyName,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String phoneNumber,
                                    Model model) throws GeneralSecurityException, IOException {


        googleContactsService.updateContact(authorizedClient, resourceName, givenName, familyName, email, phoneNumber);

        return "redirect:/contacts";
    }



    @PostMapping("/delete")
    public String deleteContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                @RequestParam String resourceName) throws GeneralSecurityException, IOException {
        googleContactsService.deleteContact(authorizedClient, resourceName);
        return "redirect:/contacts";
    }
}