/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.util;

import org.restlet.Application;
import org.restlet.security.Role;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RoleUtil {
    static final Map<String,Role> roles = Collections.synchronizedMap(new HashMap<String,Role>());

    static public Role getRoleForName(Application a, String name) {
        Role r = roles.get(name);
        if (r == null) {
            r = new Role(a, name);
            roles.put(name, r);
        }
        return r;
    }
}
