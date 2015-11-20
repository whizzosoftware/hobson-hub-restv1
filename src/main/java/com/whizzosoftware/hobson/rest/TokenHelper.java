package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.restlet.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class TokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);

    private static final String ISSUER = "Hobson";
    private static final String PROP_FIRST_NAME = "given_name";
    private static final String PROP_LAST_NAME = "family_name";
    private static final String PROP_SCOPE = "scope";
    private static final int DEFAULT_EXPIRATION_MINUTES = 60;

    static private RsaJsonWebKey rsaJsonWebKey;
    static private JwtConsumer jwtConsumer;

    static {
        // create JWT classes
        try {
            rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
            rsaJsonWebKey.setKeyId("k1");
            jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setExpectedIssuer(ISSUER)
                    .setVerificationKey(rsaJsonWebKey.getKey())
                    .build();
        } catch (JoseException e) {
            logger.error("Unable to generate RSA key for JWT");
        }
    }

    public int getDefaultExpirationMinutes() {
        return DEFAULT_EXPIRATION_MINUTES;
    }

    public String createToken(String username, Role role) {
        return createToken(username, Collections.singletonList(role));
    }

    public String createToken(String username, List<Role> scope) {
        return createToken(username, scope, DEFAULT_EXPIRATION_MINUTES);
    }

    public String createToken(String username, List<Role> roles, int expirationInMinutes) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(ISSUER);
            claims.setSubject(username);
            claims.setExpirationTimeMinutesInTheFuture(expirationInMinutes);
            claims.setStringClaim(PROP_SCOPE, StringUtils.join(roles, ","));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(rsaJsonWebKey.getPrivateKey());
            jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyType());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new HobsonAuthenticationException("Error generating token");
        }
    }

    public TokenVerification verifyToken(String token) {
        try {
            // extract the claims from the token
            JwtClaims claims = jwtConsumer.processToClaims(token);

            // make sure the token hasn't expired
            if (claims.getExpirationTime().isAfter(NumericDate.now())) {
                String scope = claims.getStringClaimValue(PROP_SCOPE);
                return new TokenVerification(
                    new HobsonUser.Builder(claims.getSubject())
                        .firstName(claims.getStringClaimValue(PROP_FIRST_NAME))
                        .lastName(claims.getStringClaimValue(PROP_LAST_NAME))
                        .build(),
                    StringUtils.split(scope)
                );
            } else {
                throw new HobsonAuthenticationException("Token has expired");
            }
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new HobsonAuthenticationException("Error validating bearer token");
        }
    }
}
