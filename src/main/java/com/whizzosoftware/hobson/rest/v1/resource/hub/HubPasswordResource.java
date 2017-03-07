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

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.PasswordChangeDTO;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
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
    AccessManager accessManager;

    @Override
    protected Representation post(Representation entity) throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_CONFIGURE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        if (!accessManager.isFederated()) {
            PasswordChangeDTO dto = new PasswordChangeDTO(JSONHelper.createJSONFromRepresentation(entity));
            accessManager.changeUserPassword("admin", DTOMapper.mapPasswordChangeDTO(dto));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonRuntimeException("Cannot change local Hub password remotely");
        }
    }
}
