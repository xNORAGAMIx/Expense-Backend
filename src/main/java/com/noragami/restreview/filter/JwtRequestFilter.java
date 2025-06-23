package com.noragami.restreview.filter;

import com.noragami.restreview.service.AppUserDetailService;
import com.noragami.restreview.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetailService appUserDetailService;
    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_URLS = List.of("/register", "/login", "/send-reset-otp", "reset-password", "/logout", "/health-check");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if(PUBLIC_URLS.contains(path)) {
            filterChain.doFilter(request,response);
            return;
        }

        String jwt = null;
        String email = null;

        // check auth header
        final String authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        // not found in header, check cookie
        if(jwt == null) {
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                for(Cookie cookie : cookies) {
                    if("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // validate token

        if(jwt != null) {
            email = jwtUtil.extractEmail(jwt);
            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
               UserDetails userDetails =  appUserDetailService.loadUserByUsername(email);
                if(jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request,response);
    }
}
