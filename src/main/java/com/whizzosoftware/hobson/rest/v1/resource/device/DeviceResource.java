/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.ExpansionFields;
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
     * @apiParam {Boolean} variables If true, then include all device variables in the response
     * @apiVersion 0.1.3
     * @apiName GetDeviceDetails
     * @apiDescription Retrieves the details of a specific device.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "RadioRa Zone 1",
     *   "type": "LIGHTBULB",
     *   "preferredVariable": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables/on"
     *   },
     *   "variables": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/variables"
     *   },
     *   "configurationClass": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/configurationClass"
     *   },
     *   "configuration": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/configuration"
     *   },
     *   "telemetry": {
     *     "@id": "/api/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/1/telemetry"
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
            .type(device.getType());

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
                HobsonVariable pv = variableManager.getDeviceVariable(device.getContext(), device.getPreferredVariableName());
                vbuilder.name(pv.getName()).mask(pv.getMask()).lastUpdate(pv.getLastUpdate()).value(pv.getValue());
            }
            builder.preferredVariable(vbuilder.build());
        }

        // set variables attribute
        ItemListDTO vdto = new ItemListDTO(linkProvider.createDeviceVariablesLink(device.getContext()));
        if (expansions.has("variables")) {
            for (HobsonVariable v : variableManager.getDeviceVariables(device.getContext()).getCollection()) {
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
