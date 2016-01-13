/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.telemetry;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.telemetry.TemporalValue;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.telemetry.TemporalValueDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;

public class TelemetryDatasetResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/telemetry/{datasetId}";

    @Inject
    @Nullable
    TelemetryManager telemetryManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/telemetry/:datasetId Get telemetry dataset
     * @apiVersion 0.8.0
     * @apiName GetTelemetryDataset
     * @apiDescription Retrieves a specific telemetry dataset.
     * @apiGroup Devices
     * @apiSuccess {String} name The name of the data set.
     * @apiSuccess {Object} data The data in the set.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/telemetry/myDataSet"
     *   "name": "tempF",
     *   "data": {
     *     "numberOfItems": 2,
     *     "itemListElement": [
     *       {
     *         "item": {
     *           "time": 1434097200,
     *           "value": 71.66541353383458
     *         }
     *       },
     *       {
     *         "item": {
     *           "time": 1434097500,
     *           "value": 70.70333333333333
     *         }
     *       }
     *     ]
     *   },
     * }
     */
    @Override
    protected Representation get() {
        if (telemetryManager != null && !telemetryManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
            String datasetId = getAttribute("datasetId");

            long endTime = System.currentTimeMillis() / 1000; // TODO: should be pulled from request
            TelemetryInterval interval = TelemetryInterval.HOURS_24; // TODO: should be pulled from request

            ItemListDTO results = new ItemListDTO(idProvider.createTelemetryDatasetId(ctx.getHubContext(), datasetId));
            Collection<TemporalValue> values = telemetryManager.getData(ctx.getHubContext(), datasetId, endTime, interval);
            for (TemporalValue tv : values) {
                results.add(new TemporalValueDTO(tv.getTime(), tv.getValue()));
            }

            JsonRepresentation jr = new JsonRepresentation(results.toJSON());
            jr.setMediaType(new MediaType(results.getJSONMediaType()));
            return jr;
        } else {
            throw new HobsonInvalidRequestException("No telemetry manager is available");
        }
    }
}
