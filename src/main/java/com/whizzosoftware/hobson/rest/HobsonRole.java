/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import org.restlet.security.Role;

/**
 * An enumeration of roles that a logged-in Hobson user may possess.
 *
 * @author Dan Noguerol
 */
public enum HobsonRole {
    ADMIN (new Role("hobsonAdmin")),
    USER (new Role("hobsonUser")),
    PROXY_USER (new Role("hobsonProxyUser"));

    private Role value;

    HobsonRole(final Role value) {
        this.value = value;
    }

    public Role value() {
        return value;
    }
}
