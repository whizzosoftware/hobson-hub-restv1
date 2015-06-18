/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.telemetry.TemporalValue;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.telemetry.TemporalValueDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;

public class DeviceTelemetryDatasetResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/telemetry/datasets/{datasetId}";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    TelemetryManager telemetryManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/telemetry/dataset/:datasetId Get device telemetry dataset
     * @apiVersion 0.5.0
     * @apiName GetDeviceTelemetryDataset
     * @apiDescription Retrieves a specific device telemetry dataset.
     * @apiGroup Devices
     * @apiSuccess {String} name The name of the data set.
     * @apiSuccess {Object} data The data in the set.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/thermostat/telemetry/datasets/tempF"
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
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        String datasetId = getAttribute("datasetId");

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        long endTime = System.currentTimeMillis() / 1000; // TODO: should be pulled from request
        TelemetryInterval interval = TelemetryInterval.HOURS_24; // TODO: should be pulled from request

        ItemListDTO results = new ItemListDTO(linkProvider.createDeviceTelemetryDatasetLink(dctx, datasetId));
        Collection<TemporalValue> values = telemetryManager.getDeviceVariableTelemetry(dctx, datasetId, endTime, interval);
        for (TemporalValue tv : values) {
            results.add(new TemporalValueDTO(tv.getTime(), tv.getValue()));
        }

        return new JsonRepresentation(results.toJSON());
    }
}
