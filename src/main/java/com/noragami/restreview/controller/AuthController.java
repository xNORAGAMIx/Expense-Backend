package com.noragami.restreview.controller;

import com.noragami.restreview.io.AuthRequest;
import com.noragami.restreview.io.AuthResponse;
import com.noragami.restreview.io.ResetPasswordRequest;
import com.noragami.restreview.service.AppUserDetailService;
import com.noragami.restreview.service.ProfileService;
import com.noragami.restreview.service.RedisService;
import com.noragami.restreview.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService appUserDetailService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final RedisService redisService;

    // User Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetailService.loadUserByUsername(request.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));
        } catch (BadCredentialsException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Email or password is incorrect.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (DisabledException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Account is disabled.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Authentication failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // Check if user is authenticated
    @GetMapping("/is-Authenticated")
    public ResponseEntity<Boolean> isAuthenticated (@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return ResponseEntity.ok(email != null);
    }

    // Send otp to user for password reset
    @PostMapping("/send-reset-otp")
    public void forgotPassword(@RequestParam String email) {
        try {
            if (redisService.isBlocked(email)) {
                System.out.println("oo many OTP requests. Try again in a minute.");
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many OTP requests. Try again in a minute.");
            }
            profileService.sendResetOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Reset the user password with help of OTP
    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            profileService.resetPassword(request.getEmail(), request.getOtp(), request.getPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Send otp to logged in user for email verification
    @PostMapping("/send-otp")
    public void sendVerifyOTP (@CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            profileService.sendOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Verify the user email
    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request, @CurrentSecurityContext(expression = "authentication?.name") String email) {
        if(request.get("otp").toString() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing details");
        }
        try {
            profileService.verifyOtp(email, request.get("otp").toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
