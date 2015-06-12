package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.user.HobsonUser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);

    private static final String ISSUER = "Hobson";
    private static final int DEFAULT_EXPIRATION_MINUTES = 60;
    private static final String PROP_FIRST_NAME = "gn";
    private static final String PROP_LAST_NAME = "sn";

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

    public String createToken(String username) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(ISSUER);
            claims.setSubject(username);
            claims.setExpirationTimeMinutesInTheFuture(DEFAULT_EXPIRATION_MINUTES);

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

    public HobsonUser verifyToken(String token) {
        try {
            // extract the claims from the token
            JwtClaims claims = jwtConsumer.processToClaims(token);

            // make sure the token hasn't expired
            if (claims.getExpirationTime().isAfter(NumericDate.now())) {
                return new HobsonUser.Builder(claims.getSubject())
                        .firstName(claims.getStringClaimValue(PROP_FIRST_NAME))
                        .lastName(claims.getStringClaimValue(PROP_LAST_NAME))
                        .build();
            } else {
                throw new HobsonAuthenticationException("Token has expired");
            }
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new HobsonAuthenticationException("Error validating bearer token");
        }
    }
}
