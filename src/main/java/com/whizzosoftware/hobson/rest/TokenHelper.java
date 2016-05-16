package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.api.user.UserAccount;
import org.apache.commons.codec.binary.Base64;
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
import sun.security.rsa.RSAPublicKeyImpl;

import java.util.*;

public class TokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);

    private static final String DEFAULT_ISSUER = "Hobson";
    private static final String DEFAULT_AUDIENCE = "hobson-api";
    private static final String PROP_FIRST_NAME = "given_name";
    private static final String PROP_LAST_NAME = "family_name";
    private static final String PROP_EXPIRATION = "expire";
    private static final String PROP_HUBS = "hubs";
    private static final int DEFAULT_EXPIRATION_MINUTES = 60;

    private String issuer;
    private static RsaJsonWebKey rsaJsonWebKey;
    private static JwtConsumer jwtConsumer;

    public TokenHelper() {
        this(System.getenv("TOKEN_PUBLIC_KEY"), System.getenv("TOKEN_ISSUER"), System.getenv("TOKEN_AUDIENCES"));
    }

    public TokenHelper(String publicKey, String issuer, String audiences) {
        try {
            if (rsaJsonWebKey == null) {
                if (publicKey != null) {
                    rsaJsonWebKey = new RsaJsonWebKey(new RSAPublicKeyImpl(Base64.decodeBase64(publicKey)));
                } else {
                    rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
                }
                rsaJsonWebKey.setKeyId("k1");
            }

            this.issuer = (issuer != null ? issuer : DEFAULT_ISSUER);

            if (jwtConsumer == null) {
                jwtConsumer = new JwtConsumerBuilder()
                        .setRequireExpirationTime()
                        .setAllowedClockSkewInSeconds(30)
                        .setRequireSubject()
                        .setVerificationKey(rsaJsonWebKey.getKey())
                        .setExpectedAudience(((audiences != null) ? DEFAULT_AUDIENCE + ",hobson-webconsole," + audiences : DEFAULT_AUDIENCE + ",hobson-webconsole").split(","))
                        .build();
            }
        } catch (Exception e) {
            logger.error("Unable to initialize keys for JWT", e);
            throw new HobsonRuntimeException("Unable to initialize keys for JWT", e);
        }
    }

    public int getDefaultExpirationMinutes() {
        return DEFAULT_EXPIRATION_MINUTES;
    }

    public String createToken(String username, Role role, Collection<String> hubs) {
        return createToken(username, Collections.singletonList(role.toString()), hubs);
    }

    private String createToken(String username, List<String> scope, Collection<String> hubs) {
        return createToken(username, scope, hubs, getDefaultExpirationMinutes());
    }

    private String createToken(String username, List<String> roles, Collection<String> hubs, int expirationInMinutes) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(issuer);
            claims.setSubject(username);
            claims.setExpirationTimeMinutesInTheFuture(expirationInMinutes);
            claims.setAudience(DEFAULT_AUDIENCE);
            claims.setClaim("realm_access", Collections.singletonMap("roles", roles));
            if (hubs != null) {
                claims.setStringClaim(PROP_HUBS, StringUtils.join(hubs, ","));
            }

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(rsaJsonWebKey.getPrivateKey());
            jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyType());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

            return jws.getCompactSerialization();
        } catch (JoseException e) {
            logger.error("Error generating token", e);
            throw new HobsonAuthenticationException("Error generating token");
        }
    }

    public TokenVerification verifyToken(String token) {
        try {
            // extract the claims from the token
            JwtClaims claims = jwtConsumer.processToClaims(token);

            // make sure the token hasn't expired
            if (claims.getExpirationTime().isAfter(NumericDate.now())) {
                Map realmAccess = claims.getClaimValue("realm_access", Map.class);
                return new TokenVerification(
                    new HobsonUser.Builder(claims.getSubject())
                        .givenName(claims.getStringClaimValue(PROP_FIRST_NAME))
                        .familyName(claims.getStringClaimValue(PROP_LAST_NAME))
                        .account(new UserAccount(claims.getStringClaimValue(PROP_EXPIRATION), claims.getStringClaimValue(PROP_HUBS)))
                        .build(),
                        (List<String>)(realmAccess != null ? realmAccess.get("roles") : new ArrayList<>())
                );
            } else {
                throw new HobsonAuthenticationException("Token has expired");
            }
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new HobsonAuthenticationException("Error validating bearer token: " + e.getMessage());
        }
    }
}
