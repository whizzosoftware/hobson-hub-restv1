/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.hub.SerialPortDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class HubSerialPortsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/serialPorts";

    @Inject
    HubManager hubManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/serialPorts Get Hub serial ports
     * @apiVersion 0.8.1
     * @apiName GetHubSerialPorts
     * @apiDescription Retrieves a list of serial ports available on the Hub.
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/serialPorts"
     *   "numberOfItems": 1,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/serialPorts/ttyUSB0"
     *       }
     *     }
     *   ]
     * }
     *
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        ItemListDTO dto = new ItemListDTO(idProvider.createHubSerialPortsId(ctx.getHubContext()));
        for (String name : hubManager.getSerialPorts(ctx.getHubContext())) {
            dto.add(new SerialPortDTO(idProvider.createHubSerialPortId(ctx.getHubContext(), name)));
        }

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }
}
