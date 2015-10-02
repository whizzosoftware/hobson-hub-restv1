/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.dto.telemetry.DeviceTelemetryDTO;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.MediaVariableProxyProvider;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

/**
 * A REST resource that returns device information.
 *
 * @author Dan Noguerol
 */
public class DeviceResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    TelemetryManager telemetryManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices/:deviceId Get device details
     * @apiVersion 0.1.3
     * @apiName GetDeviceDetails
     * @apiDescription Retrieves the details of a specific device.
     * @apiGroup Devices
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "configuration", "configurationClass", "preferredVariable", "telemetry", "variables").
     * @apiSuccess {String} name The device name.
     * @apiSuccess {String} type The device type.
     * @apiSuccess {Object} configuration The current configuration values for the device.
     * @apiSuccess {Object} configurationClass The device's configuration class.
     * @apiSuccess {Object} preferredVariable The device's "preferred variable" if it has one.
     * @apiSuccess {Object} telemetry The device's telemetry information.
     * @apiSuccess {Object} variables The variables the device has published.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "RadioRa Zone 1",
     *   "type": "LIGHTBULB",
     *   "configuration": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/configuration"
     *   },
     *   "configurationClass": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/configurationClass"
     *   },
     *   "preferredVariable": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables/on"
     *   },
     *   "telemetry": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/telemetry"
     *   },
     *   "variables": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables"
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        HobsonDevice device = deviceManager.getDevice(dctx);

        HobsonDeviceDTO.Builder builder = new HobsonDeviceDTO.Builder(linkProvider.createDeviceLink(device.getContext()))
            .name(device.getName())
            .type(device.getType())
            .available(device.isAvailable())
            .checkInTime(device.getLastCheckIn());

        // set configurationClass attribute
        PropertyContainerClassDTO.Builder pccdtob = new PropertyContainerClassDTO.Builder(linkProvider.createDeviceConfigurationClassLink(device.getContext()));
        if (expansions.has("configurationClass")) {
            PropertyContainerClass pccc = device.getConfigurationClass();
            pccdtob.supportedProperties(DTOHelper.mapTypedPropertyList(pccc.getSupportedProperties()));
        }
        builder.configurationClass(pccdtob.build());


        // set configuration attribute
        PropertyContainerDTO.Builder pcdtob = new PropertyContainerDTO.Builder(linkProvider.createDeviceConfigurationLink(device.getContext()));
        if (expansions.has("configuration")) {
            PropertyContainer config = deviceManager.getDeviceConfiguration(device.getContext());
            pcdtob.values(config.getPropertyValues());
        }
        builder.configuration(pcdtob.build());

        // set preferredVariable attribute
        if (device.hasPreferredVariableName()) {
            HobsonVariableDTO.Builder vbuilder = new HobsonVariableDTO.Builder(linkProvider.createDeviceVariableLink(device.getContext(), device.getPreferredVariableName()));
            if (expansions.has("preferredVariable")) {
                HobsonVariable pv = variableManager.getDeviceVariable(device.getContext(), device.getPreferredVariableName(), new MediaVariableProxyProvider(ctx));
                vbuilder.name(pv.getName()).mask(pv.getMask()).lastUpdate(pv.getLastUpdate()).value(pv.getValue());
            }
            builder.preferredVariable(vbuilder.build());
        }

        // set variables attribute
        ItemListDTO vdto = new ItemListDTO(linkProvider.createDeviceVariablesLink(device.getContext()));
        if (expansions.has("variables")) {
            for (HobsonVariable v : variableManager.getDeviceVariables(device.getContext(), new MediaVariableProxyProvider(ctx)).getCollection()) {
                vdto.add(new HobsonVariableDTO.Builder(linkProvider.createDeviceVariableLink(dctx, v.getName()))
                    .name(v.getName())
                    .mask(v.getMask())
                    .value(v.getValue())
                    .build()
                );
            }
        }
        builder.variables(vdto);

        // set telemetry attribute
        DeviceTelemetryDTO.Builder tdto = new DeviceTelemetryDTO.Builder(linkProvider.createDeviceTelemetryLink(device.getContext()));
        if (expansions.has("telemetry")) {
            tdto.capable(device.isTelemetryCapable());
            tdto.enabled(telemetryManager.isDeviceTelemetryEnabled(dctx));
            tdto.datasets(new ItemListDTO(linkProvider.createDeviceTelemetryDatasetsLink(dctx)));
        }
        builder.telemetry(tdto.build());

        return new JsonRepresentation(builder.build().toJSON());
    }
}
