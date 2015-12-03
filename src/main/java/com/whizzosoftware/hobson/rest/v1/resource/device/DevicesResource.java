/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
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
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/devices Get all devices
     * @apiVersion 0.1.3
     * @apiName GetAllDevices
     * @apiDescription Retrieves a summary list of devices published by all plugins.
     * @apiGroup Devices
     * @apiParam (Query Parameters) {String} var Filter the list of devices to only those that publish the specified variable name
     * @apiParam (Query Parameters) {String} type Filter the list of devices to only those of a specific type
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item", "configurationClass", "configuration", "preferredVariable", "variables").
     * @apiSuccessExample {json} Success Response:
     * {
     * "numberOfItems": 2,
     * "itemListElement": [
     * {
     * "item": {
     * "@id": "/api/plugins/v1/users/local/hubs/local/com.whizzosoftware.hobson.hub.hobson-hub-foscam/devices/camera1",
     * }
     * },
     * {
     * "item": {
     * "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/device1",
     * }
     * }
     * ]
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        String varFilter = getQueryValue("var");
        String typeFilter = getQueryValue("type");

        ItemListDTO results = new ItemListDTO(idProvider.createDevicesId(ctx.getHubContext()));

        Collection<HobsonDevice> devices = deviceManager.getAllDevices(ctx.getHubContext());
        TreeMap<String, Long> etagMap = new TreeMap<>();

        if (devices != null) {
            // TODO: refactor so the JSON isn't built if the ETag matches
            boolean itemExpand = expansions.has(JSONAttributes.ITEM);

            expansions.pushContext(JSONAttributes.ITEM);

            for (HobsonDevice device : devices) {
                if ((varFilter == null || variableManager.hasDeviceVariable(device.getContext(), varFilter)) && (typeFilter == null || device.getType().toString().equals(typeFilter))) {
                    HobsonDeviceDTO dto = new HobsonDeviceDTO.Builder(
                        dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                        device,
                        itemExpand
                    ).build();
                    results.add(dto);
                }
            }

            expansions.popContext();
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
        r.setMediaType(new MediaType(results.getJSONMediaType()));
        return r;
    }
}
