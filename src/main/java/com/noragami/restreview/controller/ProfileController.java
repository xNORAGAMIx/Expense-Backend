package com.noragami.restreview.controller;

import com.noragami.restreview.io.ProfileRequest;
import com.noragami.restreview.io.ProfileResponse;
import com.noragami.restreview.service.EmailService;
import com.noragami.restreview.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    // private final EmailService emailService;
    private final ProfileService profileService;

    // Health Check
    @GetMapping("/health-check")
    public String healthCheck() {
        return "ok";
    }

    // Register user
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        // emailService.sendWelcome(response.getEmail(), response.getName());
        return profileService.createProfile(request);
    }

    // User profile
    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return profileService.getProfile(email);
    }

    // testing api for auth
    @GetMapping("/test")
    public String test() {
        return "Auth is working";
    }

    // temporary security
    @GetMapping("/csrf")
    public CsrfToken getToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }
}
