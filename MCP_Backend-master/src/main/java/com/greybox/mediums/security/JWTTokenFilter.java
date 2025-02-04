package com.greybox.mediums.security;

import com.greybox.mediums.security.user.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class JWTTokenFilter extends OncePerRequestFilter {

    private final String PREFIX = "Bearer ";

    @Autowired
    private UserDetailsServiceImpl userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        // Get authorization header and validate
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // Get jwt token and validate
        final String token = header.split(" ")[1].trim();
        Claims claims = validateToken(token);
        if (claims == null) {
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
            return;
        }

        Object username = claims.get("username");
        if (username == null) {
            chain.doFilter(request, response);
            return;
        }

        // Get user identity and set it on the spring security context
        UserDetails userDetails = userRepo.loadUserByUsername(String.valueOf(username));

        if (userDetails != null) {
            setUpSpringAuthentication(claims, request, userDetails);
        } else {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private Claims validateToken(String jwtToken) {
        try {
            return Jwts.parser().setSigningKey("SECRET".getBytes()).parseClaimsJws(jwtToken).getBody();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Authentication method in Spring flow
     *
     * @param claims
     * @param request
     * @param userDetails
     */
    private void setUpSpringAuthentication(Claims claims, HttpServletRequest request, UserDetails userDetails) {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
