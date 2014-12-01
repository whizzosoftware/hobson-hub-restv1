/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

/**
 * A REST resource for retriving hub information.
 *
 * @author Dan Noguerol
 */
public class HubInfoResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}";

    @Inject
    HubManager hubManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId Get Hub info
     * @apiVersion 0.1.6
     * @apiName GetAPIInfo
     * @apiDescription Retrieves information about the Hub. This provides the API version number as well as links to other relevant resources.
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     * {
     *   "version": "0.1.6",
     *   "setupComplete": true,
     *   "links": {
     *     "actions": "/api/v1/users/local/hubs/local/configuration/actions",
     *     "configuration": "/api/v1/users/local/hubs/local/configuration/configuration",
     *     "devices": "/api/v1/users/local/hubs/local/devices",
     *     "log": "/api/v1/users/local/hubs/local/log",
     *     "password": "/api/v1/users/local/hubs/local/configuration/password",
     *     "presenceEntities": "/api/v1/users/local/hubs/local/presence/entities",
     *     "plugins": "/api/v1/users/local/hubs/local/plugins",
     *     "shutdown": "/api/v1/users/local/hubs/local/shutdown",
     *     "triggers": "/api/v1/users/local/hubs/local/triggers"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        try {
            // get version from properties
            Properties p = new Properties();
            p.load(getClass().getClassLoader().getResourceAsStream("version.properties"));

            HobsonRestContext context = HobsonRestContext.createContext(this, getRequest());

            // return the JSON response
            return new JsonRepresentation(
                JSONMarshaller.createApiVersionJSON(
                        context,
                        (String) p.get("version"),
                        hubManager.isSetupWizardComplete(context.getUserId(), context.getHubId())
                )
            );
        } catch (IOException e) {
            throw new HobsonRuntimeException("An error occurred retrieving Hub version", e);
        }
    }
}
