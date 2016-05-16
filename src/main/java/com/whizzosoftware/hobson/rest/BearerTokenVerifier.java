/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.security.Role;
import org.restlet.security.Verifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BearerTokenVerifier implements Verifier {
    private TokenHelper tokenHelper = new TokenHelper();

    private Application application;

    public BearerTokenVerifier(Application application) {
        this.application = application;
    }

    public int verify(Request request, Response response) {
        int result = RESULT_INVALID;

        String token = null;

        // first check challenge response
        if (request.getChallengeResponse() != null && ChallengeScheme.HTTP_OAUTH_BEARER.equals(request.getChallengeResponse().getScheme())) {
            token = request.getChallengeResponse().getRawValue();
        // then check for a cookie
        } else {
            Cookie cookie = request.getCookies().getFirst("Token", true);
            if (cookie != null) {
                token = cookie.getValue();
            }
        }

        if (token != null) {
            TokenVerification tc = tokenHelper.verifyToken(token);
            if (tc.hasUser()) {
                result = RESULT_VALID;
                request.getClientInfo().setUser(new HobsonRestUser(tc.getUser(), token));
                request.getClientInfo().setRoles(createRoles(application, tc.getRoles()));
            }
        } else {
            result = RESULT_MISSING;
        }

        return result;
    }

    private List<Role> createRoles(Application application, Collection<String> scope) {
        List<Role> roles = new ArrayList<>();
        if (scope != null) {
            for (String s : scope) {
                roles.add(application.getRole(s));
            }
        }
        return roles;
    }
}
