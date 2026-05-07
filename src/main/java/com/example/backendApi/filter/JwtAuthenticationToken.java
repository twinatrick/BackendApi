package com.example.backendApi.filter;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


@Component // 確保 Spring 可以管理這個 Filter
public class JwtAuthenticationToken {
    @Value("${jwt.secret.use}")
    private String jwtSecret; // 從配置中讀取 JWT Secret

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;

    @Value("${jwt.expiration-minutes:60}")
    private long expirationMinutes;

    private HmacKey secretKey;


    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("jwt.secret.use must be configured");
        }
        secretKey = new HmacKey(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJWT(String user
    ) throws JoseException {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", user);
        JwtClaims claims = generateClaims(map, expirationMinutes);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(secretKey);
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue("HS256");
        return jws.getCompactSerialization();
    }
    public JwtClaims generateClaims(HashMap<String, String> map, long expirationMinutes){
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(expirationMinutes);
        claims.setIssuedAtToNow();
        if (jwtIssuer != null && !jwtIssuer.isBlank()) {
            claims.setIssuer(jwtIssuer);
        }
        if (jwtAudience != null && !jwtAudience.isBlank()) {
            claims.setAudience(jwtAudience);
        }
        String subject = map.get("email");
        if (subject != null && !subject.isBlank()) {
            claims.setSubject(subject);
        }
        map.forEach(claims::setStringClaim);
        return claims;
    }

    public JwtClaims verifyJWT(String token) throws InvalidJwtException {
        JwtConsumerBuilder builder = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setVerificationKey(secretKey);
        if (jwtIssuer != null && !jwtIssuer.isBlank()) {
            builder.setExpectedIssuer(jwtIssuer);
        }
        if (jwtAudience != null && !jwtAudience.isBlank()) {
            builder.setExpectedAudience(jwtAudience);
        }
        JwtConsumer jwtConsumer = builder.build();
        try{
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            return jwtClaims;
        }catch (InvalidJwtException e) {
            throw e;
        }catch (Exception e){
            throw e;
        }
    }



}
