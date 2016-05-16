/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.user.HobsonUser;

import java.util.Collection;

/**
 * A verification result for a token which provides the user and scope associated with it.
 *
 * @author Dan Noguerol
 */
public class TokenVerification {
    private HobsonUser user;
    private Collection<String> roles;

    public TokenVerification(HobsonUser user, Collection<String> roles) {
        this.user = user;
        this.roles = roles;
    }

    public HobsonUser getUser() {
        return user;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public boolean hasUser() {
        return (user != null);
    }

    public boolean hasRole(String s) {
        return roles.contains(s);
    }
}
