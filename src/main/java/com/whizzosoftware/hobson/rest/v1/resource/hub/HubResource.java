/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.hub.HobsonHub;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource for retrieving hub information.
 *
 * @author Dan Noguerol
 */
public class HubResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}";

    @Inject
    Authorizer authorizer;
    @Inject
    HubManager hubManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId Get Hub details
     * @apiVersion 0.1.6
     * @apiName GetHubDetails
     * @apiDescription Retrieves details about a Hub. This provides the API version number as well as links to other relevant resources.
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     * {
     *   "name": "Unnamed",
     *   "version": "0.5.0",
     *   "location": {
     *     "text": "",
     *     "latitude": 0.1234,
     *     "longitude": -0.1234
     *   },
     *   "email": {
     *     "server": "localhost",
     *     "secure": false,
     *     "username": "foo",
     *     "senderAddress": "foo@bar.com"
     *   },
     *   "logLevel": "INFO",
     *   "setupComplete": true,
     *   "links": {
     *     "actions": "/api/v1/users/local/hubs/local/configuration/actions",
     *     "devices": "/api/v1/users/local/hubs/local/devices",
     *     "globalVariables": "/api/v1/users/local/hubs/local/globalVariables",
     *     "image": "/api/v1/users/local/hubs/local/image",
     *     "imageLibrary": "/api/v1/users/local/hubs/local/imageLibrary",
     *     "log": "/api/v1/users/local/hubs/local/log",
     *     "password": "/api/v1/users/local/hubs/local/password",
     *     "presenceEntities": "/api/v1/users/local/hubs/local/presence/entities",
     *     "plugins": "/api/v1/users/local/hubs/local/plugins",
     *     "self": "/api/v1/users/local/hubs/local",
     *     "shutdown": "/api/v1/users/local/hubs/local/shutdown",
     *     "tasks": "/api/v1/users/local/hubs/local/tasks"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());

        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());

        // return the JSON response
        return new JsonRepresentation(
            linkHelper.addHubDetailsLinks(
                ctx,
                JSONSerializationHelper.createHubDetailsJSON(
                    hubManager.getHub(ctx.getUserId(), ctx.getHubId())
                ),
                ctx.getHubId()
            )
        );
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId Set Hub details
     * @apiVersion 0.5.0
     * @apiName SetHubDetails
     * @apiDescription Sets details about a hub.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "name": "Test Hub",
     *   "email": {
     *     "server": "smtp.mydomain.com",
     *     "secure": true,
     *     "senderAddress": "foo@bar.com",
     *     "username": "user",
     *     "password": "password"
     *   },
     *   "location": {
     *     "text": "555 Some St, New York, NY 10021",
     *     "latitude": 0.1234,
     *     "longitude": 0.1234,
     *   },
     *   "logLevel": "INFO",
     *   "setupComplete": false
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        HobsonHub hub = JSONSerializationHelper.createHubDetails(getAttribute("hubId"), JSONHelper.createJSONFromRepresentation(entity));
        hubManager.setHubDetails(ctx.getUserId(), ctx.getHubId(), hub);
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    /**
     * @api {delete} /api/v1/users/:userId/hubs/:hubId Delete Hub details
     * @apiVersion 0.5.0
     * @apiName DeleteHubDetails
     * @apiDescription Deletes any user-defined details about a hub.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation delete() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        hubManager.clearHubDetails(ctx.getUserId(), ctx.getHubId());
        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

}
