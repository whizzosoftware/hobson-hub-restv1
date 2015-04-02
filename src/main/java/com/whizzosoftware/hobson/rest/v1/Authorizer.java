/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

/**
 * An interface for performing user authorization.
 *
 * @author Dan Noguerol
 */
public interface Authorizer {
    /**
     * Verifies whether a user is authorized to acccess a hub.
     *
     * @param userId the user ID making the request
     * @param hubId the hub ID being accessed
     *
     * @throws com.whizzosoftware.hobson.api.HobsonAuthException if authorization fails
     *
     */
    public void authorizeHub(String userId, String hubId);
}
