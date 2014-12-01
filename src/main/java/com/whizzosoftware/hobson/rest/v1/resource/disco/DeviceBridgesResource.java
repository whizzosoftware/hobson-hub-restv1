/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.disco;

import com.whizzosoftware.hobson.api.disco.DiscoManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for retrieving a list of device bridges.
 *
 * @author Dan Noguerol
 */
public class DeviceBridgesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/deviceBridges";

    @Inject
    DiscoManager discoManager;

    /**
     * @api {get} /api/deviceBridges Get device bridges
     * @apiVersion 0.1.3
     * @apiName GetAllDeviceBridges
     * @apiDescription Retrieves a summary list of all device bridges published by plugins.
     * @apiGroup Discovery
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "type": "philipsHueBridge",
     *     "name": "Philips Hue Bridge [192.168.0.220]",
     *     "value": "192.168.0.220"
     *   }
     * ]
     */
    @Override
    public Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        return new JsonRepresentation(JSONMarshaller.getDeviceBridgeListJSON(ctx, discoManager.getDeviceBridges(ctx.getUserId(), ctx.getHubId())));
    }
}
