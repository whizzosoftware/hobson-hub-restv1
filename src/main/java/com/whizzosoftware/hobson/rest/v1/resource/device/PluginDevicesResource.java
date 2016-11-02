/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.*;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for obtaining a plugin's devices.
 *
 * @author Dan Noguerol
 */
public class PluginDevicesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices";

    @Inject
    PluginManager pluginManager;
    @Inject
    DeviceManager deviceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/devices Get all plugin devices
     * @apiVersion 0.1.3
     * @apiName GetAllPluginDevices
     * @apiDescription Retrieves all devices published by a specific plugin.
     * @apiGroup Devices
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (the only supported value is "item").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/device1"
     *     },
     *     {
     *       "item": {
     *         "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/device2"
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        boolean showDetails = expansions.has(JSONAttributes.ITEM);

        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        ItemListDTO results = new ItemListDTO(idProvider.createPluginDevicesId(pctx));

        expansions.pushContext(JSONAttributes.ITEM);

        for (HobsonDeviceDescriptor device : deviceManager.getDevices(PluginContext.create(ctx.getHubContext(), getAttribute("pluginId")))) {
            results.add(
                new HobsonDeviceDTO.Builder(
                    dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions),
                    device.getContext(),
                    showDetails
                ).build()
            );
        }

        expansions.popContext();

        JsonRepresentation jr = new JsonRepresentation(results.toJSON());
        jr.setMediaType(new MediaType(results.getJSONMediaType()));
        return jr;
    }

//    @Override
//    protected Representation post(Representation entity) {
//        final HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
//        final PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
//        final HobsonDeviceDTO dto = new HobsonDeviceDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
//        final DeviceType type = dto.getType();
//
//        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
//            @Override
//            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
//                return deviceManager.getPluginDeviceType(pctx, type).getConfig();
//            }
//        };
//
//        AddDeviceResult adr = deviceManager.addDevice(pctx, type, dto.getName(), DTOMapper.mapPropertyContainerDTO(dto.getConfiguration(), pccp, idProvider));
//        getResponse().setLocationRef(idProvider.createJobId(ctx.getHubContext(), adr.getJobId()));
//        return new EmptyRepresentation();
//    }

}
