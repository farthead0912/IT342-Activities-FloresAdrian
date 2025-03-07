package com.flores.oauth2login.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.flores.oauth2login.service.GoogleContactsService;

@Controller
public class TestController {
    @Autowired
    GoogleContactsService googleContactsService;

    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes();
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, OAuth2AuthenticationToken authenticationToken) {
        List<Map<String, Object>> contacts = googleContactsService.getContacts(authenticationToken);
        model.addAttribute("contacts", contacts);
        return "contacts";  // Render Thymeleaf template
    }
}