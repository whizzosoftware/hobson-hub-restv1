/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import com.whizzosoftware.hobson.api.user.HobsonUser;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A verification result for a token which provides the user and scope associated with it.
 *
 * @author Dan Noguerol
 */
public class TokenVerification {
    private HobsonUser user;
    private String[] scope;

    public TokenVerification(HobsonUser user, String[] scope) {
        this.user = user;
        this.scope = scope;
    }

    public HobsonUser getUser() {
        return user;
    }

    public String[] getScope() {
        return scope;
    }

    public boolean hasUser() {
        return (user != null);
    }

    public boolean hasScope(String s) {
        return ArrayUtils.contains(scope, s);
    }
}
