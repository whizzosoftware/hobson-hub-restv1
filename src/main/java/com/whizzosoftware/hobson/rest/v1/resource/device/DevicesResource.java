/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.ExpansionFields;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOHelper;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
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
    public static final String REL = "devices";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    HATEOASLinkProvider linkHelper;

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

        ItemListDTO results = new ItemListDTO(linkHelper.createDevicesLink(ctx.getHubContext()));

        boolean itemExpand = expansions.has("item");
        for (HobsonDevice device : deviceManager.getAllDevices(ctx.getHubContext())) {
            HobsonDeviceDTO dto = new HobsonDeviceDTO(linkHelper.createDeviceLink(device.getContext()), itemExpand ? device.getName() : null);
            if (itemExpand) {
                dto.setType(device.getType());

                // set configurationClass attribute
                PropertyContainerClassDTO pccdto = new PropertyContainerClassDTO(linkHelper.createDeviceConfigurationClassLink(device.getContext()));
                if (expansions.has("configurationClass")) {
                    PropertyContainerClass pccc = device.getConfigurationClass();
                    pccdto.setSupportedProperties(DTOHelper.mapTypedPropertyList(pccc.getSupportedProperties()));
                }
                dto.setConfigurationClass(pccdto);

                // set configuration attribute
                PropertyContainerDTO pcdto = new PropertyContainerDTO(linkHelper.createDeviceConfigurationLink(device.getContext()));
                if (expansions.has("configuration")) {
                    PropertyContainer config = deviceManager.getDeviceConfiguration(device.getContext());
                    pcdto.setPropertyValues(config.getPropertyValues());
                }
                dto.setConfiguration(pcdto);

                // set preferredVariable attribute
                if (device.hasPreferredVariableName()) {
                    HobsonVariableDTO.Builder vbuilder = new HobsonVariableDTO.Builder(linkHelper.createDeviceVariableLink(device.getContext(), device.getPreferredVariableName()));
                    if (expansions.has("preferredVariable")) {
                        HobsonVariable pv = variableManager.getDeviceVariable(device.getContext(), device.getPreferredVariableName());
                        vbuilder.name(pv.getName()).mask(pv.getMask()).lastUpdate(pv.getLastUpdate()).value(pv.getValue());
                    }
                    dto.setPreferredVariable(vbuilder.build());
                }

                // set variables attribute
                ItemListDTO vdto = new ItemListDTO(linkHelper.createDeviceVariablesLink(device.getContext()));
                if (expansions.has("variables")) {
                    for (HobsonVariable v : variableManager.getDeviceVariables(device.getContext()).getCollection()) {
                        vdto.add(new HobsonVariableDTO.Builder(linkHelper.createDeviceVariableLink(device.getContext(), v.getName()))
                            .name(v.getName())
                            .mask(v.getMask())
                            .value(v.getValue())
                            .build()
                        );
                    }
                }
                dto.setVariables(vdto);
            }
            results.add(dto);
        }
        return new JsonRepresentation(results.toJSON(linkHelper));
    }
}
