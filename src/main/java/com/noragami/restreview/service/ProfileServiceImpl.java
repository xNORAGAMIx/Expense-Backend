package com.noragami.restreview.service;

import com.noragami.restreview.entity.UserEntity;
import com.noragami.restreview.events.ResetPasswordEvent;
import com.noragami.restreview.events.UserRegisteredEvent;
import com.noragami.restreview.io.ProfileRequest;
import com.noragami.restreview.io.ProfileResponse;
import com.noragami.restreview.kafka.KafkaEventProducer;
import com.noragami.restreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final KafkaEventProducer kafkaEventProducer;


    // Register User
    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity user = convertToUserEntity(request);
        if (!userRepository.existsByEmail(user.getEmail())) {
            user = userRepository.save(user);

            // Send Kafka event
            kafkaEventProducer.sendUserRegisteredEvent(
                    new UserRegisteredEvent(user.getName(), user.getEmail())
            );
            return convertToProfileResponse(user);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists!");
    }

    // Fetch User profile
    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found "+email));
        return convertToProfileResponse(user);
    }

    // Send otp for password reset
    @Override
    public void sendResetOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));
        // Generate otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Expiry time
        long expiry = System.currentTimeMillis() + (10 * 60 * 1000);

        // update the user entity
        user.setResetOTP(otp);
        user.setResetOtpExpireAt(expiry);

        // save to database
        userRepository.save(user);

        try {
              //emailService.sendResetEmail(user.getEmail(), otp);
            kafkaEventProducer.sendResetPasswordEvent(
                    new ResetPasswordEvent(user.getEmail(),user.getResetOTP())
            );
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send mail");
        }
    }

    // Reset user password
    @Override
    public void resetPassword(String email, String otp, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));

        if(user.getResetOTP() == null || !user.getResetOTP().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if(user.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP Expired");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetOTP(null);
        user.setResetOtpExpireAt(0L);

        userRepository.save(user);
    }

    // Send otp for email verification
    @Override
    public void sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));

        if(user.getIsAccountVerified() != null && user.getIsAccountVerified()) {
            return;
        }

        // Generate otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Expiry time
        long expiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        // update user
        user.setVerifyOTP(otp);
        user.setVerifyOtpExpireAt(expiry);

        userRepository.save(user);

        try {
            emailService.sendVerifyEmail(user.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send mail");
        }
    }

    // Verify otp for email verification
    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));

        if(user.getVerifyOTP() == null || !user.getVerifyOTP().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if(user.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP Expired");
        }

        user.setIsAccountVerified(true);
        user.setVerifyOTP(null);
        user.setVerifyOtpExpireAt(0L);

        userRepository.save(user);
    }

    @Override
    public String getLoggedInUserId(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: "+email));
        return  user.getUserId();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
       return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .verifyOTP(null)
                .verifyOtpExpireAt(0L)
                .resetOTP(null)
                .resetOtpExpireAt(0L)
                .isAccountVerified(false)
               .build();
    }

    private ProfileResponse convertToProfileResponse(UserEntity user) {
       return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .isAccountVerified(user.getIsAccountVerified())
               .build();
    }
}
