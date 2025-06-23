package com.noragami.restreview.service;

import com.noragami.restreview.entity.UserEntity;
import com.noragami.restreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
       UserEntity user = userRepository.findByEmail(email)
               .orElseThrow(() -> new UsernameNotFoundException("User not found for the email."+email));
       return new User(user.getEmail(),user.getPassword(), new ArrayList<>());
    }
}
