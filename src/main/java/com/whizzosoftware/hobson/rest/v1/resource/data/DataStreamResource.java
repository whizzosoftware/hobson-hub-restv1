/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.data;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.data.TelemetryManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.data.DataStreamDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class DataStreamResource extends SelfInjectingServerResource {
    public static final String PATH = "/dataStreams/{dataStreamId}";

    @Inject
    TelemetryManager telemetryManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId/dataStreams/:dataStreamId Get data stream
     * @apiVersion 0.8.0
     * @apiName GetDataStream
     * @apiDescription Retrieve details about a telemetry data stream.
     * @apiGroup Telemetry
     * @apiSuccess {String} name The name of the data stream.
     * @apiSuccess {Object} variables The variables in the data stream.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/dataStreams/31c68ded-2364-4fb0-9bee-9d96b388476a"
     *   "name": "Temperature Data",
     *   "fields": [
     *       {
     *         "name": "Outdoor Temperature",
     *         "variable": {
     *           "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/wstation/variables/outTempF"
     *         }
     *       },
     *       {
     *         "name": "Indoor Temperature",
     *         "variable": {
     *           "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/thermostat/variables/inTempF"
     *         }
     *       }
     *   ],
     *   "links": {
     *     "data": "/api/v1/users/local/hubs/local/dataStreams/31c68ded-2364-4fb0-9bee-9d96b388476a/data"
     *   }
     * }
     */
    @Override
    protected Representation get() {
        if (telemetryManager != null && !telemetryManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
            ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
            DataStreamDTO dto = new DataStreamDTO.Builder(
                dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                telemetryManager.getDataStream(getAttribute("dataStreamId")), // TODO
                true
            ).build();
            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(new MediaType(dto.getJSONMediaType()));
            return jr;
        } else {
            throw new HobsonInvalidRequestException("No telemetry manager is available");
        }
    }
}
