/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.user.UserStore;
import com.whizzosoftware.hobson.dto.PasswordChangeDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that manages the Hub admin password.
 *
 * @author Dan Noguerol
 */
public class HubPasswordResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/password";

    @Inject
    UserStore userStore;

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        if (!isInRole(HobsonRole.administrator.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        if (userStore.supportsUserManagement()) {
            PasswordChangeDTO dto = new PasswordChangeDTO(JSONHelper.createJSONFromRepresentation(entity));
            userStore.changeUserPassword("admin", DTOMapper.mapPasswordChangeDTO(dto));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonRuntimeException("Cannot change local Hub password remotely");
        }
    }
}
