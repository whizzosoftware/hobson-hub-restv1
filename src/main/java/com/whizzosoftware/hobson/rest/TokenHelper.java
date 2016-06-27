package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.api.user.UserAccount;
import com.whizzosoftware.hobson.rest.oidc.OIDCConfig;
import com.whizzosoftware.hobson.rest.oidc.OIDCConfigProvider;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.lang.JoseException;
import org.restlet.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(TokenHelper.class);

    private static final String PROP_FIRST_NAME = "given_name";
    private static final String PROP_LAST_NAME = "family_name";
    private static final String PROP_EXPIRATION = "expire";
    private static final String PROP_HUBS = "hubs";
    private static final int DEFAULT_EXPIRATION_MINUTES = 60;

    static public String createToken(OIDCConfigProvider provider, HobsonUser user, Role role, Collection<String> hubs) {
        return createToken(provider, user, Collections.singletonList(role.toString()), hubs);
    }

    static private String createToken(OIDCConfigProvider provider, HobsonUser user, List<String> scope, Collection<String> hubs) {
        return createToken(provider, user, scope, hubs, DEFAULT_EXPIRATION_MINUTES);
    }

    static private String createToken(OIDCConfigProvider provider, HobsonUser user, List<String> roles, Collection<String> hubs, int expirationInMinutes) {
        try {
            OIDCConfig config = provider.getConfig();

            JwtClaims claims = new JwtClaims();
            claims.setIssuer(config.getIssuer());
            claims.setAudience("hobson-webconsole");
            claims.setSubject(user.getId());
            claims.setStringClaim(PROP_FIRST_NAME, user.getGivenName());
            claims.setStringClaim(PROP_LAST_NAME, user.getFamilyName());
            claims.setExpirationTimeMinutesInTheFuture(expirationInMinutes);
            claims.setClaim("realm_access", Collections.singletonMap("roles", roles));
            if (hubs != null) {
                claims.setStringClaim(PROP_HUBS, StringUtils.join(hubs, ","));
            }

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(config.getSigningKey().getPrivateKey());
            jws.setKeyIdHeaderValue(config.getSigningKey().getKeyType());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

            return jws.getCompactSerialization();
        } catch (JoseException e) {
            logger.error("Error generating token", e);
            throw new HobsonAuthenticationException("Error generating token");
        }
    }

    static public TokenVerification verifyToken(JwtConsumer jwtConsumer, String token) {
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
