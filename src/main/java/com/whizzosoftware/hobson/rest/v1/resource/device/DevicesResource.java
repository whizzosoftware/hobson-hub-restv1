/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
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
public class DevicesResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/devices";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/devices Get all devices
     * @apiVersion 0.1.3
     * @apiName GetAllDevices
     * @apiDescription Retrieves a summary list of devices published by all plugins.
     * @apiGroup Devices
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/plugins/v1/users/local/hubs/local/com.whizzosoftware.hobson.server-radiora/devices/1",
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.server-radiora/devices/2",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        ItemListDTO results = new ItemListDTO(linkProvider.createDevicesLink(ctx.getHubContext()));

        boolean itemExpand = expansions.has("item");
        for (HobsonDevice device : deviceManager.getAllDevices(ctx.getHubContext())) {
            HobsonDeviceDTO.Builder builder = new HobsonDeviceDTO.Builder(linkProvider.createDeviceLink(device.getContext()));
            if (itemExpand) {
                builder.name(device.getName());
                builder.type(device.getType());

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
                        vdto.add(new HobsonVariableDTO.Builder(linkProvider.createDeviceVariableLink(device.getContext(), v.getName()))
                            .name(v.getName())
                            .mask(v.getMask())
                            .value(v.getValue())
                            .build()
                        );
                    }
                }
                builder.variables(vdto);
            }
            results.add(builder.build());
        }
        return new JsonRepresentation(results.toJSON());
    }
}
