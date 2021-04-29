package com.devilpanda.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class JwtProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProvider.class);
    private static final String JWT_SIGN_SECRET = "STUB_SECRET";

    public String getUserNameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SIGN_SECRET)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SIGN_SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(JWT_SIGN_SECRET).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.warn("Expired Jwt Token -> Message: {}", e.getMessage());
        } catch (SignatureException e) {
            LOGGER.warn("Invalid Jwt signature -> Message: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.warn("Unsupported Jwt Token -> Message: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.warn("Malformed Jwt Token -> Message: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Illegal argument for Jwt Token -> Message: {}", e.getMessage());
        }
        return false;
    }

    private String toJsonString(Serializable object) {
        ObjectWriter writer = new ObjectMapper().writer();
        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format("Could not transform object '%s' to JSON: ", object), e);
        }
    }
}
