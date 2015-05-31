package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.user.HobsonUser;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.security.User;
import org.restlet.security.Verifier;

public class BearerTokenVerifier implements Verifier {
    TokenHelper tokenHelper = new TokenHelper();

    public int verify(Request request, Response response) {
        int result = RESULT_INVALID;

        if (request.getResourceRef().getPath().endsWith("/login")) {
            result = RESULT_VALID;
        } else {
            String token = null;

            // first check cookie
            Cookie cookie = request.getCookies().getFirst("Token", true);
            if (cookie != null) {
                token = cookie.getValue();
                // then check challenge response
            } else if (request.getChallengeResponse() != null && ChallengeScheme.HTTP_OAUTH_BEARER.equals(request.getChallengeResponse().getScheme())) {
                token = request.getChallengeResponse().getRawValue();
            }

            if (token != null) {
                HobsonUser user = tokenHelper.verifyToken(token);
                if (user != null) {
                    result = RESULT_VALID;
                    request.getClientInfo().setUser(
                        new User(
                            user.getId(),
                            token,
                            user.getFirstName(),
                            user.getLastName(),
                            null
                        )
                    );
                }
            } else {
                result = RESULT_MISSING;
            }
        }

        return result;
    }
}