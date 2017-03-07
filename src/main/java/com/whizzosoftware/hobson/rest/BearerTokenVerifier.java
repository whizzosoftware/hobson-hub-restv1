/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.api.security.HobsonUser;
import com.whizzosoftware.hobson.rest.v1.util.RoleUtil;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.security.Role;
import org.restlet.security.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BearerTokenVerifier implements Verifier {
    private Logger logger = LoggerFactory.getLogger(BearerTokenVerifier.class);

    private Application application;
    private AccessManager accessManager;

    public BearerTokenVerifier(Application application, AccessManager accessManager) {
        this.application = application;
        this.accessManager = accessManager;
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
            try {
                HobsonUser user = accessManager.authenticate(token);
                if (user != null) {
                    result = RESULT_VALID;
                    request.getClientInfo().setUser(new HobsonRestUser(user, token));
                    request.getClientInfo().setRoles(getRestletRoles(application, user.getRoles()));
                }
            } catch (HobsonAuthenticationException hae) {
                logger.debug("Error verifying token: " + hae);
            } catch (Exception e) {
                logger.error("Error verifying token", e);
            }
        } else {
            result = RESULT_MISSING;
        }

        return result;
    }

    private List<Role> getRestletRoles(Application a, Collection<String> roles) {
        List<Role> r = new ArrayList<>();
        for (String role : roles) {
            r.add(RoleUtil.getRoleForName(a, role));
        }
        return r;
    }
}
