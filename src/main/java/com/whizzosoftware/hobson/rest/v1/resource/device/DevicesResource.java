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
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.CRC32;

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
     * @apiParam (Query Parameters) {String} var Filter the list of devices to only those that publish the specified variable name
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item", "configurationClass", "configuration", "preferredVariable", "variables").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/plugins/v1/users/local/hubs/local/com.whizzosoftware.hobson.hub.hobson-hub-foscam/devices/camera1",
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/device1",
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        String varFilter = getQueryValue("var");

        authorizer.authorizeHub(ctx.getHubContext());

        ItemListDTO results = new ItemListDTO(linkProvider.createDevicesLink(ctx.getHubContext()));

        Collection<HobsonDevice> devices = deviceManager.getAllDevices(ctx.getHubContext());
        TreeMap<String,Long> etagMap = new TreeMap<>();

        // TODO: refactor so the JSON isn't built if the ETag matches

        boolean itemExpand = expansions.has("item");
        for (HobsonDevice device : devices) {
            if (varFilter == null || variableManager.hasDeviceVariable(device.getContext(), varFilter)) {
                HobsonDeviceDTO.Builder builder = new HobsonDeviceDTO.Builder(linkProvider.createDeviceLink(device.getContext()));
                long lastVariableUpdate = 0;
                if (itemExpand) {
                    builder.name(device.getName());
                    builder.type(device.getType());
                    builder.available(device.isAvailable());
                    builder.checkInTime(device.getLastCheckIn());

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
                            if (pv.getLastUpdate() > lastVariableUpdate) {
                                lastVariableUpdate = pv.getLastUpdate();
                            }
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
                            if (v.getLastUpdate() > lastVariableUpdate) {
                                lastVariableUpdate = v.getLastUpdate();
                            }
                        }
                    }
                    builder.variables(vdto);

                }
                results.add(builder.build());
                etagMap.put(device.getContext().toString(), device.isAvailable() ? lastVariableUpdate : -1);
            }
        }

        // the ETag is a CRC calculated from all devices' contexts and last variable updates
        CRC32 crc = new CRC32();
        for (String dctx : etagMap.keySet()) {
            String s = dctx + Long.toString(etagMap.get(dctx));
            crc.update(s.getBytes());
        }
        Tag etag = new Tag(Long.toString(crc.getValue()));

        // check if ETag matches request
        List<Tag> requestTags = getRequest().getConditions().getNoneMatch();
        Representation r;
        if (requestTags.size() == 0 || !requestTags.get(0).equals(etag)) {
            r = new JsonRepresentation(results.toJSON());
        } else {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
            r = new EmptyRepresentation();
        }

        r.setTag(etag);
        return r;
    }
}
