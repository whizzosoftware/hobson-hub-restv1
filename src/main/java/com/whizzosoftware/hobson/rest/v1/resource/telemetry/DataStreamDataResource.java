/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.telemetry;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.persist.PropertyConstants;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.telemetry.DataStreamDataDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class DataStreamDataResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/dataStreams/{dataStreamId}/data";

    @Inject
    TelemetryManager telemetryManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/dataStreams/:dataStreamId/data Get data stream data
     * @apiVersion 0.8.0
     * @apiName GetDataStreamData
     * @apiDescription Retrieve the data for a telemetry data stream.
     * @apiGroup Telemetry
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/dataStreams/31c68ded-2364-4fb0-9bee-9d96b388476a/data",
     *   "endTime": 1434097500,
     *   "interval": "HOURS_24",
     *   "data": [
     *     {
     *       "timestamp": 1434097200,
     *       "com.whizzosoftware.hobson.hub.hobson-hub-sample:thermostat:inTempF": 71.6,
     *       "com.whizzosoftware.hobson.hub.hobson-hub-sample:wstation:outTempF": 42.5
     *     },
     *     {
     *       "timestamp": 1434097500,
     *       "com.whizzosoftware.hobson.hub.hobson-hub-sample:thermostat:inTempF": 70.7,
     *       "com.whizzosoftware.hobson.hub.hobson-hub-sample:wstation:outTempF": 42.6
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        if (telemetryManager != null && !telemetryManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

            String dataStreamId = getAttribute(PropertyConstants.DATA_STREAM_ID);

            long endTime;
            String s = getQueryValue("endTime");
            if (s != null) {
                endTime = Long.parseLong(s);
            } else {
                endTime = System.currentTimeMillis();
            }

            TelemetryInterval inr;
            s = getQueryValue("inr");
            if (s != null) {
                inr = TelemetryInterval.valueOf(s);
            } else {
                inr = TelemetryInterval.HOURS_1;
            }

            DataStreamDataDTO dto = new DataStreamDataDTO(
                idProvider.createDataStreamDataId(ctx.getUserId(), dataStreamId),
                endTime,
                inr,
                telemetryManager.getData(ctx.getUserId(), dataStreamId, endTime, inr)
            );

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(new MediaType(dto.getJSONMediaType()));
            return jr;
        } else {
            throw new HobsonInvalidRequestException("No telemetry manager is available");
        }
    }
}
