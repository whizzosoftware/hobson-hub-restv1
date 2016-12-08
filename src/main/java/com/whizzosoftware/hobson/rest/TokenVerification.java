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

import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.rest.v1.util.RoleUtil;
import org.restlet.Application;
import org.restlet.security.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A verification result for a token which provides the user and scope associated with it.
 *
 * @author Dan Noguerol
 */
public class TokenVerification {
    private HobsonUser user;
    private Collection<String> hubs;
    private Collection<HobsonRole> roles;

    public TokenVerification(HobsonUser user, Collection<String> hubs, Collection<HobsonRole> roles) {
        this.user = user;
        this.hubs = hubs;
        this.roles = roles;
    }

    public HobsonUser getUser() {
        return user;
    }

    public Collection<String> getHubs() {
        return hubs;
    }

    public Collection<HobsonRole> getRoles() {
        return roles;
    }

    public List<Role> getRestletRoles(Application a) {
        List<Role> roles = new ArrayList<>();
        for (HobsonRole r : this.roles) {
            roles.add(RoleUtil.getRoleForName(a, r.name()));
        }
        return roles;
    }

    public boolean hasUser() {
        return (user != null);
    }

    public boolean hasRole(HobsonRole role) {
        return roles.contains(role);
    }
}
