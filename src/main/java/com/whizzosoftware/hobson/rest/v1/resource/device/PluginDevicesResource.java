/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.*;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
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
    AccessManager accessManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    DeviceManager deviceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);
        final boolean showDetails = expansions.has(JSONAttributes.ITEM);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DEVICE_READ, null);

        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        ItemListDTO dto = new ItemListDTO(bctx.getIdProvider().createPluginDevicesId(pctx).getId());

        expansions.pushContext(JSONAttributes.ITEM);

        for (HobsonDeviceDescriptor device : deviceManager.getDevices(PluginContext.create(ctx.getHubContext(), getAttribute("pluginId")))) {
            dto.add(
                new HobsonDeviceDTO.Builder(
                    bctx,
                    device.getContext(),
                    showDetails
                ).build()
            );
        }

        expansions.popContext();

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }
}
