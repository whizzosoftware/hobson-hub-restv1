/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.config;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.util.UserUtil;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that manages Hub configuration.
 *
 * @author Dan Noguerol
 */
public class HubConfigurationResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/configuration";
    public static final String REL = "configuration";

    @Inject
    HubManager hubManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/configuration Get Hub configuration
     * @apiVersion 0.1.7
     * @apiName GetHubConfiguration
     * @apiDescription Retrieves the current Hub configuration.
     * @apiGroup Hub
     * @apiSuccessExample {json} Success Response:
     * {
     *   "email": {
     *     "server": "smtp.mydomain.com",
     *     "secure": true,
     *     "senderAddress": "foo@bar.com",
     *     "username": "user"
     *   },
     *   "location": {
     *     "text": "555 Some St, New York, NY 10021",
     *     "latitude": 0.1234,
     *     "longitude": 0.1234,
     *   },
     *   "logLevel": "INFO",
     *   "name": "My Hub Name",
     *   "setupComplete": true
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(
            JSONMarshaller.createHubConfigurationJSON(
                ctx,
                hubManager.getHubName(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB),
                hubManager.getHubEmailConfiguration(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB),
                hubManager.getHubLocation(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB),
                hubManager.getLogLevel(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB),
                hubManager.isSetupWizardComplete(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB)
            )
        );
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/configuration Set Hub configuration
     * @apiVersion 0.1.7
     * @apiName SetHubConfiguration
     * @apiDescription Updates the current Hub configuration. Note that this can be a full or partial representation.
     * @apiGroup Hub
     * @apiParamExample {json} Example Request:
     * {
     *   "location": {
     *     "text": "555 Some St, New York, NY 10021",
     *     "latitude": 0.1234,
     *     "longitude": 0.1234,
     *   },
     *   "name": "My Hub Name"
     * }
     * @apiSuccessExample {json} Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        JSONObject json = JSONMarshaller.createJSONFromRepresentation(entity);

        if (json.has("email")) {
            hubManager.setHubEmailConfiguration(ctx.getUserId(), ctx.getHubId(), JSONMarshaller.createEmailConfiguration(json.getJSONObject("email")));
        }
        if (json.has("location")) {
            hubManager.setHubLocation(ctx.getUserId(), ctx.getHubId(), JSONMarshaller.createHubLocation(json.getJSONObject("location")));
        }
        if (json.has("logLevel")) {
            hubManager.setLogLevel(ctx.getUserId(), ctx.getHubId(), json.getString("logLevel"));
        }
        if (json.has("name")) {
            hubManager.setHubName(ctx.getUserId(), ctx.getHubId(), json.getString("name"));
        }
        if (json.has("setupComplete")) {
            hubManager.setSetupWizardComplete(ctx.getUserId(), ctx.getHubId(), json.getBoolean("setupComplete"));
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}
