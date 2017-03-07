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

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.MapUtil;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;

import javax.inject.Inject;

/**
 * A REST resource for shutting down the Hub.
 *
 * @author Dan Noguerol
 */
public class ShutdownResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/shutdown";

    @Inject
    AccessManager accessManager;
    @Inject
    IdProvider idProvider;

    @Override
    public Representation post(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_EXECUTE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        getResponse().setLocationRef(new Template(HubResource.PATH).format(MapUtil.createEmptyMap(ctx)));
        Representation result = new EmptyRepresentation();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
                System.exit(0);
            }
        });
        t.start();

        return result;
    }
}
