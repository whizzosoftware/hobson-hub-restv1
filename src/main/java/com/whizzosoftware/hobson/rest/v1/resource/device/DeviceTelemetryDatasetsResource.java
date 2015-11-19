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
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.telemetry.TemporalValue;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.telemetry.TelemetryDatasetDTO;
import com.whizzosoftware.hobson.dto.telemetry.TemporalValueDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;

public class DeviceTelemetryDatasetsResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/telemetry/datasets";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    TelemetryManager telemetryManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/telemetry/datasets Get device telemetry datasets
     * @apiVersion 0.5.0
     * @apiName GetDeviceTelemetryDatasets
     * @apiDescription Retrieves all telemetry datasets for a specific device.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/plugin1/devices/device1/telemetry/datasets/tempF"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/plugin1/devices/device1/telemetry/datasets/targetTempF"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        HobsonDevice device = deviceManager.getDevice(dctx);

        ItemListDTO datasets = new ItemListDTO(idProvider.createDeviceTelemetryDatasetsId(dctx));

        String[] vars = device.getTelemetryVariableNames();
        for (String var : vars) {
            long endTime = System.currentTimeMillis() / 1000; // TODO: should be pulled from request
            TelemetryInterval interval = TelemetryInterval.HOURS_24; // TODO: should be pulled from request
            TelemetryDatasetDTO.Builder builder = new TelemetryDatasetDTO.Builder(idProvider.createDeviceTelemetryDatasetId(dctx, var));
            builder.name(var);
            if (expansions.has("item")) {
                Collection<TemporalValue> data = telemetryManager.getDeviceVariableTelemetry(dctx, var, endTime, interval);
                ItemListDTO dataDto = new ItemListDTO(null);
                for (TemporalValue tv : data) {
                    dataDto.add(new TemporalValueDTO(tv.getTime(), tv.getValue()));
                }
                builder.data(dataDto);
            }
            datasets.add(builder.build());
        }

        return new JsonRepresentation(datasets.toJSON());
    }
}
