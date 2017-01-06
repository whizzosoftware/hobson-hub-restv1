/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.hub.OIDCConfig;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);

    private static final String PROP_FIRST_NAME = "given_name";
    private static final String PROP_LAST_NAME = "family_name";
    private static final String PROP_HUBS = "hubs";
    private static final int DEFAULT_EXPIRATION_MINUTES = 60;

    private static JwtConsumer jwtConsumer;

    static public String createToken(OIDCConfig config, HobsonUser user, Collection<HobsonRole> roles, Collection<String> hubs) {
        return createToken(config, user, roles, hubs, DEFAULT_EXPIRATION_MINUTES);
    }

    static private String createToken(OIDCConfig config, HobsonUser user, Collection<HobsonRole> roles, Collection<String> hubs, int expirationInMinutes) {
        try {
            List<String> r = new ArrayList<>();
            for (HobsonRole hr : roles) {
                r.add(hr.name());
            }

            JwtClaims claims = new JwtClaims();
            claims.setIssuer(config.getIssuer());
            claims.setAudience(System.getenv("OIDC_AUDIENCE") != null ? System.getenv("OIDC_AUDIENCE") : System.getProperty("OIDC_AUDIENCE", "hobson-webconsole"));
            claims.setSubject(user.getId());
            claims.setStringClaim(PROP_FIRST_NAME, user.getGivenName());
            claims.setStringClaim(PROP_LAST_NAME, user.getFamilyName());
            claims.setExpirationTimeMinutesInTheFuture(expirationInMinutes);
            claims.setClaim("realm_access", Collections.singletonMap("roles", r));
            if (hubs != null) {
                claims.setStringClaim(PROP_HUBS, StringUtils.join(hubs, ","));
            }

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(((RsaJsonWebKey)config.getSigningKey()).getPrivateKey());
            jws.setKeyIdHeaderValue(((RsaJsonWebKey)config.getSigningKey()).getKeyType());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

            return jws.getCompactSerialization();
        } catch (JoseException e) {
            logger.error("Error generating token", e);
            throw new HobsonAuthenticationException("Error generating token");
        }
    }

    static public HobsonUser verifyToken(OIDCConfig config, String token) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(TokenHelper.class.getClassLoader());
        try {
            // extract the claims from the token
            JwtClaims claims = getJwtConsumer(config).processToClaims(token);

            // make sure the token hasn't expired
            if (claims.getExpirationTime().isAfter(NumericDate.now())) {
                List<String> roles = null;
                Map realmAccess = claims.getClaimValue("realm_access", Map.class);
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    roles = (List<String>)realmAccess.get("roles");
                }
                return new HobsonUser.Builder(claims.getSubject())
                    .givenName(claims.getStringClaimValue(PROP_FIRST_NAME))
                    .familyName(claims.getStringClaimValue(PROP_LAST_NAME))
                    .roles(roles != null ? roles : new ArrayList<String>())
                    .hubs(Collections.singletonList(claims.getClaimValue("hubs", String.class)))
                    .build();
            } else {
                throw new HobsonAuthenticationException("Token has expired");
            }
        } catch (Exception e) {
            throw new HobsonAuthenticationException("Error validating bearer token: " + e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    static private JwtConsumer getJwtConsumer(OIDCConfig oidcConfig) throws Exception {
        if (jwtConsumer == null) {
            if (oidcConfig != null) {
                jwtConsumer = new JwtConsumerBuilder()
                        .setRequireExpirationTime()
                        .setAllowedClockSkewInSeconds(30)
                        .setRequireSubject()
                        .setExpectedIssuer(oidcConfig.getIssuer())
                        .setVerificationKey(((RsaJsonWebKey)oidcConfig.getSigningKey()).getKey())
                        .setExpectedAudience(System.getenv("OIDC_AUDIENCE") != null ? System.getenv("OIDC_AUDIENCE") : System.getProperty("OIDC_AUDIENCE", "hobson-webconsole"))
                        .build();
            }
        }
        return jwtConsumer;
    }

}
