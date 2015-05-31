/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource;

import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubResource;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
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
    public static final String PATH = "/users/{userId}/hubs/{hubId}/shutdown";
    public static final String REL = "shutdown";

    @Inject
    Authorizer authorizer;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/shutdown Shutdown
     * @apiVersion 0.1.6
     * @apiName Shutdown
     * @apiDescription Attempts to shutdown the Hub.
     *
     * The response header Location facilitates polling to determine when the server has successfully shut down.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     * Location: /api/v1/users/local/hubs/local
     */
    @Override
    public Representation post(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        getResponse().setLocationRef(new Template(HubResource.PATH).format(linkHelper.createEmptyMap(ctx)));
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
