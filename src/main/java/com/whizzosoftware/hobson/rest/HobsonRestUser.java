/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.user.HobsonUser;
import org.restlet.security.User;

import java.util.Collection;

public class HobsonRestUser extends User {
    private HobsonUser user;
    private Collection<String> hubs;

    HobsonRestUser(HobsonUser user, String token, Collection<String> hubs) {
        super(user.getId(), token != null ? token.toCharArray() : null, user.getGivenName(), user.getFamilyName(), user.getEmail());
        this.user = user;
        this.hubs = hubs;
    }

    public HobsonUser getUser() {
        return user;
    }

    public Collection<String> getHubs() {
        return hubs;
    }
}
