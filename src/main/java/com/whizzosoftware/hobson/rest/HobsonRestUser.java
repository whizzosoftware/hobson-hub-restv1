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

    HobsonRestUser(HobsonUser user, String token) {
        super(user.getId(), token != null ? token.toCharArray() : null, user.getGivenName(), user.getFamilyName(), user.getEmail());
        this.user = user;
    }

    public HobsonUser getUser() {
        return user;
    }
}
