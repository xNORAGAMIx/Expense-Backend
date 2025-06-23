package com.noragami.restreview.service;

import com.noragami.restreview.io.ProfileRequest;
import com.noragami.restreview.io.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String password);

    // Email verification

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    String getLoggedInUserId(String email);
}
