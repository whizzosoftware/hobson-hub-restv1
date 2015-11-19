/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.telemetry.DeviceTelemetryDTO;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.telemetry.TelemetryDatasetDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that returns device telemetry data.
 *
 * @author Dan Noguerol
 */
public class DeviceTelemetryResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/telemetry";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    TelemetryManager telemetryManager;
    @Inject
    VariableManager variableManager;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/telemetry Get device telemetry info
     * @apiVersion 0.1.8
     * @apiName GetDeviceTelemetry
     * @apiDescription Retrieves telemetry information for a specific device.
     * @apiGroup Devices
     * @apiSuccess {Boolean} capable Indicates if the device can provide telemetry data.
     * @apiSuccess {Boolean} enabled Indicates if the device is currently providing telemetry data.
     * @apiSuccess {Object} datasets The data sets the device can provide.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "capable": true,
     *   "enabled": true,
     *   "datasets": {
     *     "@id": "/api/v1/users/local/hubs/local/plugins/plugin1/devices/device1/telemetry/datasets"
     *   }
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
        DeviceTelemetryDTO.Builder builder = new DeviceTelemetryDTO.Builder(idProvider.createDeviceTelemetryId(dctx));
        builder.capable(device.isTelemetryCapable()).enabled(telemetryManager.isDeviceTelemetryEnabled(dctx)).datasets(datasets);

        if (expansions.has("datasets")) {
            String[] vars = device.getTelemetryVariableNames();
            for (String var : vars) {
                datasets.add(new TelemetryDatasetDTO.Builder(idProvider.createDeviceTelemetryDatasetId(dctx, var)).build());
            }
        }

        return new JsonRepresentation(builder.build().toJSON());
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId/telemetry Enable device telemetry
     * @apiVersion 0.1.8
     * @apiName SetDeviceTelemetry
     * @apiDescription Enabled/disables telemetry for a specific device.
     * @apiGroup Devices
     * @apiExample Example Request:
     * {
     *   "enabled": true
     * }
     * @apiSuccessExample Success Response:
     * HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        String pluginId = getAttribute("pluginId");
        String deviceId = getAttribute("deviceId");

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        if (json.has("enabled")) {
            telemetryManager.enableDeviceTelemetry(DeviceContext.create(ctx.getHubContext(), pluginId, deviceId), json.getBoolean("enabled"));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("enabled is a required field");
        }
    }
}
