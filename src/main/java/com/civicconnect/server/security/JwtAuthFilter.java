package com.civicconnect.server.security;

import com.civicconnect.server.entity.User;
import com.civicconnect.server.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Intercepts every single HTTP request to check for a Supabase JWT in the Authorization header.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final SecretKey key;

    public JwtAuthFilter(UserRepository userRepository,
                         @Value("${app.supabase.jwt-secret:this_is_a_dummy_secret_key_long_enough_for_hs256_testing}") String jwtSecret) {
        this.userRepository = userRepository;
        // Supabase JWTs are signed using HMAC-SHA256 with your project's JWT Secret.
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract token from header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 2. Parse and validate the token signature using Supabase secret
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 3. Get the user ID (Supabase stores it in the "sub" claim)
            String userId = claims.getSubject();

            // 4. Load the user from our database to get their actual Role
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    // 5. Wrap the user in our CustomUserDetails
                    CustomUserDetails userDetails = new CustomUserDetails(user);

                    // 6. Tell Spring Security: "This user is authenticated!"
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Placing it here allows the Controller to use @AuthenticationPrincipal CustomUserDetails
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid, expired, or tampered with
            // We just let the filter chain continue; Spring Security will block it later
            // because SecurityContextHolder is still empty.
        }

        // 7. Pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}
